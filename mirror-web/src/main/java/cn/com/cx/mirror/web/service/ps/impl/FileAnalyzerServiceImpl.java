package cn.com.cx.mirror.web.service.ps.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.stereotype.Service;

import cn.com.cx.mirror.web.service.ps.FileAnalyzerService;
import cn.com.cx.ps.mirror.utils.AstUtils;
import cn.com.cx.ps.mirror.utils.FileUtils;

@Service
public class FileAnalyzerServiceImpl implements FileAnalyzerService {

    @Override
    public Map<String, CompilationUnit> extractCompilationUnits(Set<String> javaFiles) {
        Map<String, CompilationUnit> map = new HashMap<>();
        for (String javaFile : javaFiles) {
            CompilationUnit unit = parserCompilationUnit(javaFile);
            map.put(javaFile, unit);
        }
        return map;
    }

    @Override
    public CompilationUnit parserCompilationUnit(String javaFile) {
        return AstUtils.getCompUnitResolveBinding(javaFile);
    }

	@Override
	public List<String> extractLineNums(String filePath) {
		return FileUtils.listCodeLines(filePath);
	}
    

}
