package cn.com.cx.ps.mirror.project;

import cn.com.cx.ps.mirror.project.variable.Class;
import cn.com.cx.ps.mirror.tools.runner.ClassAnalyzerRunner;
import cn.com.cx.ps.mirror.tools.runner.FileAnalyzerRunner;
import cn.com.cx.ps.mirror.tools.runner.PackageAnalyzerRunner;
import cn.com.cx.ps.mirror.exceptions.ProjectException;
import cn.com.cx.ps.mirror.tools.visitor.VariableVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MirrorProject {
    private static Logger logger = LoggerFactory.getLogger(MirrorProject.class);

    /**
     * project location
     */
    private File prjPath;
    /**
     * the number of all java files
     */
    private int fileCount;
    /**
     * project name
     */
    private String prjName;

    /**
     * all java files in the project
     */
    private Set<String> prjJavaFiles = new HashSet<>();

    private Map<String, ClassFile> prjClassesFile = new HashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(13);

    public MirrorProject(File prjDir, String prjName) throws ProjectException {
        if (!prjDir.isDirectory()) {
            throw new ProjectException(prjDir.getAbsolutePath() + " is not a dir.");
        }
        this.prjName = prjName;
        this.prjPath = prjDir;
        initjavaFiles(prjDir);
        logger.info("Java file count: [{}]", this.getFileCount());
    }

    /**
     * <p>extract all the java file in the project and store it in the property of @see prjJavaFiles.</p>
     * <p>get all files in this project ant the count of the files(".java")</p>
     *
     * @param dir
     */
    private void initjavaFiles(File dir) {
        File[] fs = dir.listFiles();
        String tmpPath = null;
        for (int i = 0; i < fs.length; i++) {
            if (fs[i].getAbsolutePath().endsWith(".java")) {
                tmpPath = fs[i].getAbsolutePath();
                this.fileCount += 1;
                prjJavaFiles.add(tmpPath);
            }
            if (fs[i].isDirectory()) {
                initjavaFiles(fs[i]);
            } // end if
        } // end for
    }

    public void startFileAnalyzer() {
//        initialized, start to analyze
        FileAnalyzerRunner fileAnalyzer = new FileAnalyzerRunner(this.prjJavaFiles);
        Future<?> fileSubmit = executorService.submit(fileAnalyzer);
        if (fileSubmit.isDone()) {
            PackageAnalyzerRunner packageAnalyzer = new PackageAnalyzerRunner(fileAnalyzer.getPrjCompUnits());
            Future<?> pkgSubmit = executorService.submit(packageAnalyzer);

            ClassAnalyzerRunner classAnalyzer = new ClassAnalyzerRunner(fileAnalyzer.getPrjCompUnits());
            Future<?> clsSubmit = executorService.submit(classAnalyzer);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            重构，至此，继续Variable解析和存储变量
            if (pkgSubmit.isDone() && clsSubmit.isDone()) {
                VariableVisitor variableVisitor = null;
                for (String tmpPath : prjJavaFiles) {
                    variableVisitor = new VariableVisitor(tmpPath, this);

                    ClassFile classFile = new ClassFile(tmpPath);
                    classFile.getCompilationUnit().accept(variableVisitor);
                    classFile.setVariablesInFile(variableVisitor.getVariables());

                    this.prjClassesFile.put(tmpPath, classFile);
                }
            }
        }

    }


    public File getPrjPath() {
        return prjPath;
    }

    public int getFileCount() {
        return fileCount;
    }

    public Map<String, ClassFile> getPrjClassesFile() {
        return prjClassesFile;
    }

    public boolean classInProject(String qualifiedClassName) {
        for (ClassFile classFile : this.prjClassesFile.values()) {
            for (Class customizedClass : classFile.getClassesInFile()) {
                if (customizedClass.getQualifiedName().contains(qualifiedClassName)) return true;
                continue;
            }
        }
        return false;
    }

    public String getPrjName() {
        return prjName;
    }

    public void setPrjName(String prjName) {
        this.prjName = prjName;
    }

    public void setPrjPath(File prjPath) {
        this.prjPath = prjPath;
    }

    public Set<String> getPrjJavaFiles() {
        return prjJavaFiles;
    }

    public void setPrjJavaFiles(Set<String> prjJavaFiles) {
        this.prjJavaFiles = prjJavaFiles;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
}