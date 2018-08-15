package cn.com.mirror.analyser.visitor;

import cn.com.mirror.project.unit.element.Class;
import cn.com.mirror.project.unit.element.Variable;
import cn.com.mirror.project.unit.element.VariableType;
import cn.com.mirror.project.unit.element.VariableType.PRIME;
import cn.com.mirror.project.unit.element.VariableType.TYPE;
import cn.com.mirror.utils.AstUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * The type Variable visitor. visit all SimpleNodes and resolve its binding and
 * extract the variables for each line of codes
 * <p>
 * Extract all the variables in the project file.
 */
@Getter
@Setter
@Slf4j
public class VariableVisitor extends ASTVisitor {
    private final String file;
    private final String packageName;
    /**
     * all classes defined in the project
     */
    private final Map<String, Set<Class>> prjClasses;

    private Set<Variable> variableSet = new HashSet<>(); // all element defined in this project file
    private Map<Integer, Set<Variable>> variableInFile = new TreeMap<>();

    public VariableVisitor(String file, String packageName, Map<String, Set<Class>> prjClasses) {
        this.file = file;
        this.packageName = packageName;
        this.prjClasses = prjClasses;
    }

    @Override
    public boolean visit(SimpleName node) {
        IBinding binding = node.resolveBinding();
        if (binding instanceof IVariableBinding) {
            IVariableBinding varTypeBinding = (IVariableBinding) binding;
            Variable variable = new Variable();
            variable.setAstNode(node);
            variable.setFile(file);
            variable.setLineNum(AstUtils.getEndLine(node));
            variable.setName(varTypeBinding.getName());
            variable.setField(varTypeBinding.isField());

            // Variable type handle AND TYPE ONLY
            variable.setVariableType(analysisVariableType(varTypeBinding.getType()));

            addVariable(AstUtils.getEndLine(node), variable);

            variableSet.add(variable);
        }
        return super.visit(node);
    }


// private methods
    private void addVariable(Integer lineNum, Variable variable) {
        if (!variableInFile.containsKey(lineNum)) {
            Set<Variable> set = new HashSet<>();
            set.add(variable);
            variableInFile.put(lineNum, set);
        } else {
            variableInFile.get(lineNum).add(variable);
        }
    }

    /**
     * check the qualified class name is included in this project, if not then skip
     * it if so just initializing this class element type into the project object.
     */
    private VariableType analysisVariableType(ITypeBinding typeBinding) {
        VariableType varType = null;
        switch (VariableType.TYPE.judgeType(typeBinding)) {
            case PRIME:
                varType = new VariableType(TYPE.PRIME, PRIME.prime(typeBinding.getName()));
                break;

            case CLASS:
                String clsTypeQualifiedName = typeBinding.getQualifiedName();

                if (classDefinedInProject(prjClasses, clsTypeQualifiedName)) {
                    varType = new VariableType(TYPE.CLASS, clsTypeQualifiedName);
                } else {
                    varType = new VariableType(TYPE.OTHER, clsTypeQualifiedName);
                }
                break;

            case INTERFACE:
                varType = new VariableType(TYPE.INTERFACE, typeBinding.getQualifiedName());
                break;
            case ENUM:
                varType = new VariableType(TYPE.ENUM, typeBinding.getQualifiedName());
                break;

            case ARRAY:
                StringBuilder builder = new StringBuilder(typeBinding.getQualifiedName());
                String tmType = builder.substring(0, builder.lastIndexOf("["));

                VariableType eleType = new VariableType(TYPE.OTHER);
                if (classDefinedInProject(prjClasses, tmType)) {
                    eleType = new VariableType(TYPE.CLASS, tmType);
                } else if (PRIME.isPRIME(tmType)) {
                    eleType = new VariableType(TYPE.PRIME, PRIME.prime(tmType));
                }
                varType = new VariableType(TYPE.ARRAY, eleType);
                break;

            case OTHER:
                log.warn("VARIABLE OTHER TYPE: [{}]", typeBinding);
                break;
            default:
                break;
        }
        return varType;
    }

    private boolean classDefinedInProject(Map<String, Set<Class>> prjClasses, String qualifiedClassName) {

        Set<Entry<String, Set<Class>>> entrySet = prjClasses.entrySet();
        Iterator<Entry<String, Set<Class>>> classIterator = entrySet.iterator();
        while (classIterator.hasNext()) {
            Entry<String, Set<Class>> nextClass = classIterator.next();
            Set<Class> clsValSet = nextClass.getValue();
            for (Class clsVal : clsValSet) {
                if (clsVal.getQualifiedName().contains(qualifiedClassName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
