/**
 * 
 */
package cn.com.cx.ps.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.com.cx.ps.mirror.MirrorServiceApplication;
import cn.com.cx.ps.mirror.common.utils.AstUtils;
import cn.com.cx.ps.mirror.common.utils.MirrorTestProperties;
import cn.com.cx.ps.mirror.common.visitor.ClassDeclarationVisitor;
import cn.com.cx.ps.mirror.common.visitor.VariableVisitor;
import cn.com.cx.ps.mirror.java.variable.Class;

/**
 * @author Piggy
 *
 * @description 
 * @date 2018年2月1日
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes= MirrorServiceApplication.class)
public class VariableVisitorTests {
	
	@Autowired
	private MirrorTestProperties mirrorTestProperties;
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Test
	public void testVariable() {
		String path = mirrorTestProperties.getFile();
		CompilationUnit compilationUnit = AstUtils.getCompUnitResolveBinding(path);
		log.info(path);
		ClassDeclarationVisitor classDeclarationVisitor = new ClassDeclarationVisitor(path);
		compilationUnit.accept(classDeclarationVisitor);
		Map<String, Set<Class>> classMap = new HashMap<>();
		classMap.put(path, classDeclarationVisitor.getPrjClasses());
		VariableVisitor variableVisitor = new VariableVisitor(mirrorTestProperties.getFile(), classMap);
		compilationUnit.accept(variableVisitor);
		
		variableVisitor.getVariables();
		
		log.info("Variables: {}", variableVisitor.getVariables());
	}

}