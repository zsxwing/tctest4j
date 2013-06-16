package me.iamzsx.tctest4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.topcoder.client.contestApplet.common.LocalPreferences;
import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.DataType;
import com.topcoder.shared.problem.Renderer;
import com.topcoder.shared.problem.TestCase;

/**
 * 
 * @author zsxwing
 * 
 */
public class TCTestForJava {

    private File getTestFile(ProblemComponentModel problem) {
        String dirName = LocalPreferences.getInstance().getProperty(
                "fileeditor.config.dirName");
        if (dirName == null || dirName.isEmpty()) {
            dirName = ".";
        }
        return new File(dirName, problem.getClassName() + "Test.java");
    }

    public String preProcess(String source, ProblemComponentModel problem,
            Language lang, Renderer renderer) {
        if (!problem.hasSignature()) {
            return source;
        }

        if (!"Java".equals(lang.getName())) {
            return source;
        }

        File testFile = getTestFile(problem);
        if (testFile.exists()) {
            return "";
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(testFile));
            printImports(writer);
            writer.println();
            printClassStart(writer, problem.getClassName() + "Test");
            writer.println();
            if (problem.getTestCases() != null) {
                int exampleNo = 0;
                for (TestCase testCase : problem.getTestCases()) {
                    printTestCase(writer, problem, lang, exampleNo,
                            problem.getParamTypes(), problem.getParamNames(),
                            testCase);
                    exampleNo++;
                    writer.println();
                }
            }
            printClassEnd(writer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        return source;
    }

    private void printImports(PrintWriter writer) {
        writer.println("import java.util.*;\n");
        writer.println("import org.junit.*;\n");
    }

    private void printClassStart(PrintWriter writer, String className) {
        writer.println("public class " + className + " {");
    }

    private void printClassEnd(PrintWriter writer) {
        writer.println("}");
    }

    private void printTestCase(PrintWriter writer,
            ProblemComponentModel problem, Language language, int exampleNo,
            DataType[] paramType, String[] paramName, TestCase testCase) {
        indent(writer, "@Test");
        indent(writer, "public void testExample" + exampleNo + "() {");
        String[] paramValue = testCase.getInput();
        for (int i = 0; i < paramValue.length; i++) {
            printDeclarement(writer, language, paramType[i], paramName[i],
                    paramValue[i]);
        }
        printDeclarement(writer, language, problem.getReturnType(), "output",
                testCase.getOutput());
        printAssert(writer, problem);
        indent(writer, "}");
    }

    private void printDeclarement(PrintWriter writer, Language language,
            DataType paramType, String paramName, String paramValue) {
        StringBuilder statement = new StringBuilder();
        statement.append(paramType.getDescriptor(language));
        statement.append(" ");
        statement.append(paramName);
        statement.append(" = ");
        statement.append(fixValueForLong(paramType, paramValue));
        statement.append(";");
        indent(writer, 2, statement.toString());
    }

    private void printAssert(PrintWriter writer, ProblemComponentModel problem) {
        StringBuilder statement = new StringBuilder();
        if (problem.getReturnType().getDimension() == 0) {
            statement.append("Assert.assertEquals(output, ");
        } else {
            statement.append("Assert.assertArrayEquals(output, ");
        }
        statement.append("new ");
        statement.append(problem.getClassName());
        statement.append("().");
        statement.append(problem.getMethodName());
        statement.append("(");
        for (int i = 0; i < problem.getParamNames().length; i++) {
            if (i != 0) {
                statement.append(", ");
            }
            statement.append(problem.getParamNames()[i]);
        }
        statement.append("));");
        indent(writer, 2, statement.toString());
    }

    private void indent(PrintWriter writer, String statement) {
        indent(writer, 1, statement);
    }

    private void indent(PrintWriter writer, int indentCount, String statement) {
        for (int i = 0; i < indentCount; i++) {
            writer.print(INDENT);
        }
        writer.println(statement);
    }

    private String fixValueForLong(DataType dt, String val) {
        if (dt.getBaseName().toLowerCase().indexOf("long") != -1) {
            val = val.replaceAll("\\d+", "$0L");
        }
        if (dt.getDimension() != 0) {
            val = val.replaceAll("\n", "\n           " + INDENT);
        }
        return val;
    }

    private static final String INDENT = "    ";

}
