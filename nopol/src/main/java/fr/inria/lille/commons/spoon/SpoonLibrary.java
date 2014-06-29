package fr.inria.lille.commons.spoon;

import java.io.File;
import java.util.List;

import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.compiler.SpoonCompiler;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.CodeFactory;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import com.martiansoftware.jsap.JSAPException;

import fr.inria.lille.commons.classes.ClassLibrary;
import fr.inria.lille.commons.collections.ListLibrary;

public class SpoonLibrary {
	
	public static Factory modelFor(File sourceFile) {
		Factory factory = newFactory();
		factory.getEnvironment().setDebug(true);
		try {
			SpoonCompiler compiler = launcher().createCompiler(factory);
			compiler.addInputSource(sourceFile);
			compiler.addTemplateSource(sourceFile);
			compiler.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return factory;
	}
	
	public static CtBreak newBreak(Factory factory) {
		return factory.Core().createBreak();
	}
	
	public static <T> CtLiteral<T> newLiteral(Factory factory, T value) {
		CtLiteral<T> newLiteral = factory.Core().createLiteral();
		newLiteral.setValue(value);
		return newLiteral;
	}
	
	public static <T> CtLocalVariable<T> newLocalVariableDeclaration(Factory factory, String classSimpleName, String variableName, T defaultValue) {
		CtTypeReference<T> type = factory.Core().createTypeReference();
		type.setSimpleName(classSimpleName);
		CtLiteral<T> defaultExpression = factory.Core().createLiteral();
		defaultExpression.setValue(defaultValue);
		return factory.Code().createLocalVariable(type, variableName, defaultExpression);
	}
	
	public static <T> CtExpression<T> newExpressionFromSnippet(Factory factory, String codeSnippet, Class<T> expressionClass) {
		return factory.Code().createCodeSnippetExpression(codeSnippet);
	}
	
	public static CtStatement newStatementFromSnippet(Factory factory, String codeSnippet) {
		return factory.Code().createCodeSnippetStatement(codeSnippet);
	}
	
	public static CtBlock<CtStatement> newBlock(Factory factory, CtStatement... statements) {
		CtBlock<CtStatement> newBlock = factory.Core().createBlock();
		setParent(newBlock, statements);
		List<CtStatement> blockStatements = ListLibrary.newArrayList(statements);
		newBlock.setStatements(blockStatements);
		return newBlock;
	}
	
	public static CtExpression<Boolean> newConjunctionExpression(Factory factory, CtExpression<Boolean> leftExpression, CtExpression<Boolean> rightExpression) {
		return newComposedExpression(factory, leftExpression, rightExpression, BinaryOperatorKind.AND);
	}
	
	public static <T> CtExpression<T> newComposedExpression(Factory factory, CtExpression<T> leftExpression, CtExpression<T> rightExpression, BinaryOperatorKind operator) {
		CtBinaryOperator<T> composedExpression = factory.Code().createBinaryOperator(leftExpression, rightExpression, operator);
		setParent(composedExpression, leftExpression, rightExpression);
		return composedExpression;
	}
	
	public static CtIf newIf(Factory factory, CtExpression<Boolean> condition, CtStatement thenBranch, CtStatement elseBranch) {
		CtIf newIf = factory.Core().createIf();
		setParent(newIf, condition, thenBranch, elseBranch);
		newIf.setCondition(condition);
		newIf.setThenStatement(thenBranch);
		newIf.setElseStatement(elseBranch);
		return newIf;
	}
	
	public static void setParent(CtElement parent, CtElement... children) {
		for (CtElement child : children) {
			child.setParent(parent);
		}
	}
	
	public static boolean isBlock(CtElement element) {
		return ClassLibrary.isInstanceOf(CtBlock.class, element);
	}
	
	public static boolean isLocalVariable(CtElement element) {
		return ClassLibrary.isInstanceOf(CtLocalVariable.class, element);
	}
	
	public static boolean isAnonymousClass(CtElement element) {
		return ClassLibrary.isInstanceOf(CtNewClass.class, element);
	}
	
	public static boolean isConstructor(CtElement element) {
		return ClassLibrary.isInstanceOf(CtConstructor.class, element);
	}
	
	public static boolean isInitializationBlock(CtElement element) {
		return ClassLibrary.isInstanceOf(CtAnonymousExecutable.class, element);
	}
	
	public static boolean isAType(CtElement element) {
		return ClassLibrary.isInstanceOf(CtSimpleType.class, element);
	}
	
	public static boolean isField(CtElement element) {
		return ClassLibrary.isInstanceOf(CtField.class, element);
	}
	
	public static boolean allowsModifiers(CtElement element) {
		return ClassLibrary.isInstanceOf(CtModifiable.class, element);
	}
	
	public static boolean hasStaticModifier(CtElement element) {
		if (allowsModifiers(element)) {
			return ClassLibrary.castTo(CtModifiable.class, element).getModifiers().contains(ModifierKind.STATIC);
		}
		return false;
	}
	
	public static boolean inStaticCode(CtElement element) {
		if (allowsModifiers(element)) {
			return hasStaticModifier(element);
		}
		return hasStaticModifier(element.getParent(CtModifiable.class));
	}
	
	public static CtStatement statementOf(CtCodeElement codeElement) {
		Class<CtStatement> statementClass = CtStatement.class;
		if (ClassLibrary.isInstanceOf(statementClass, codeElement)) {
			return ClassLibrary.castTo(statementClass, codeElement);
		}
		return codeElement.getParent(statementClass);
	}
	
	public static CoreFactory coreFactoryOf(CtElement element) {
		return element.getFactory().Core();
	}
	
	public static CodeFactory codeFactoryOf(CtElement element) {
		return element.getFactory().Code();
	}
	
	public static Environment newEnvironment() {
		return newFactory().getEnvironment();
	}
	
	public static Factory newFactory() {
		return launcher().createFactory();
	}
	
	private static Launcher launcher() {
		if (launcher == null) {
			try {
				launcher = new Launcher();
			} catch (JSAPException e) {
				e.printStackTrace();
			}
		}
		return launcher;
	}
	
	private static Launcher launcher;
}
