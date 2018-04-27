package cn.com.cx.ps.mirror.visitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.springframework.util.Assert;

import cn.com.cx.ps.mirror.java.variable.Class;
import cn.com.cx.ps.mirror.java.variable.Variable;
import cn.com.cx.ps.mirror.java.variable.VariableType;
import cn.com.cx.ps.mirror.java.variable.VariableType.PRIME;
import cn.com.cx.ps.mirror.java.variable.VariableType.TYPE;
import cn.com.cx.ps.mirror.utils.AstUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Variable visitor. visit all SimpleNodes and resolve its binding and
 * extract the variables for each line of codes
 * <p>
 * Extract all the variables in the java file.
 * <p>
 * Extract all the variables in one line.
 */
@Getter
@Setter
@Slf4j
public class VariableVisitor extends ASTVisitor {
	/**
	 * java file name
	 */
	private String file;
	/**
	 * package name of the file
	 */
	private String packageName;

	private Map<String, Set<Class>> prjClasses; // all classes defined in the project
	private Set<Variable> varSet = new HashSet<>(); // all variable defined in this java file
	private Map<Integer, Set<Variable>> varInFile;

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

			varSet.add(variable);
		}
		return super.visit(node);
	}

	private void addVariable(Integer lineNum, Variable variable) {
		if (null == this.varInFile) {
			// this.varInFile = new HashMap<>();
			this.varInFile = new TreeMap<>();
		}

		if (!varInFile.containsKey(lineNum)) {
			Set<Variable> set = new HashSet<>();
			set.add(variable);
			varInFile.put(lineNum, set);
		} else {
			varInFile.get(lineNum).add(variable);
		}
	}

	/**
	 * check the qualified class name is included in this project, if not then skip
	 * it if so just initializing this class variable type into the project object.
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
		Assert.notNull(prjClasses, "project classes parameter can not be NULL");

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