package cn.com.mirror.repository.neo4j.node;

import cn.com.mirror.constant.EdgeType;
import lombok.Data;
import org.neo4j.ogm.annotation.*;

import java.io.Serializable;

/**
 * @author piggy
 * @description
 * @date 18-8-9
 */
@Data
@NodeEntity(label = "a node in target which contains some basic information")
public class Node implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Property(name = "line num")
    private Integer lineNum;

    @Property(name = "target path")
    private String targetPath;

    @Property(name = "node type")
    private String nodeType;

    @Relationship(type = EdgeType.EDGE_TYPE.NODE_TO_NODE_CTRL_EDGE)
    private Node ctrlDepNode;

}
