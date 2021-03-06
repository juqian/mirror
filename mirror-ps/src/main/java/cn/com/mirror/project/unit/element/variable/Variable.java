package cn.com.mirror.project.unit.element.variable;

import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;

import lombok.Data;

/**
 * The type Variable. A element which defined and used in the project is
 * unique.
 */
@Data
public class Variable implements Comparable<Variable> {
    private String name;
    private VariableType variableType;

    private boolean fieldFlag;
    private boolean paramFlag;
    private String file;
    private int lineNum;
    private ASTNode astNode;

    @Override
    public int hashCode() {
        return Objects.hash(name, variableType, fieldFlag, file);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Variable that = (Variable) obj;
        return fieldFlag == that.isFieldFlag() &&
                Objects.equals(name, that.getName()) &&
                Objects.equals(variableType, that.getVariableType()) &&
                Objects.equals(file, that.getFile());
    }

    @Override
    public int compareTo(Variable o) {
        if (lineNum < o.getLineNum()) return -1;
        if (lineNum > o.getLineNum()) return 1;
        return 0;
    }
}
