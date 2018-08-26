/**
 *
 */
package cn.com.mirror.constant;

import cn.com.mirror.exceptions.ConstantException;
import lombok.Getter;

/**
 * @author Piggy
 * @description
 * @date 2018年4月27日
 */
@Getter
public enum NodeTypeEnum {
    ROOT("ROOT"), // package
    CLASS("CLASS"),
    METHOD("METHOD"),
    STATEMENT("STATEMENT");

    private String key;

    private NodeTypeEnum(String key) {
        this.key = key;
    }

    public static NodeTypeEnum getNodeTypeEnum(String key) {
        for (NodeTypeEnum nodeTypeEnum : NodeTypeEnum.values()) {
            if (nodeTypeEnum.getKey().equals(key)) {
                return nodeTypeEnum;
            }
        }
        throw new ConstantException("No node type match!");
    }

}
