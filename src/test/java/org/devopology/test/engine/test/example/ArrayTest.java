package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.TestEngine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ArrayTest {

    @TestEngine.ParameterInject
    public String[] parameter;

    @TestEngine.ParameterSupplier
    public static Stream<String[]> parameters() {
        Collection<String[]> collection = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            collection.add(new String[] { java.lang.String.valueOf(i), java.lang.String.valueOf(i * 2) });
        }

        return collection.stream();
    }

    private static PrintStream filePrintStream;

    @TestEngine.BeforeClass
    public static void beforeClass() throws FileNotFoundException {
        filePrintStream = new PrintStream(new FileOutputStream("ArrayTest.txt", true));
        log("beforeClass()");
    }

    @TestEngine.BeforeEachParameter
    public void beforeEachParameter() {
        log("beforeEachParameter()");
    }

    @TestEngine.BeforeEachTest
    public void beforeEachTest() {
        log("beforeEachTest()");
    }

    @TestEngine.Test
    public void test1() {
        log("test()");
        log("test1(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.Test
    public void test2() {
        log("test()");
        log("test2(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.Test
    public void test3() {
        log("test3()");
        log("test3(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.AfterEachTest
    public void afterEachTest() {
        log("afterEachTest()");
    }

    @TestEngine.AfterEachParameter
    public void afterEachParameter() {
        log("afterEachParameter()");
    }

    @TestEngine.AfterClass
    public static void afterClass() {
        log("afterClass()");
        if (filePrintStream != null) {
            try {
                filePrintStream.close();
            } catch (Throwable t) {
                // DO NOTHING
            }
        }
    }

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private static void log(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        synchronized (SIMPLE_DATE_FORMAT) {
            stringBuilder.append(SIMPLE_DATE_FORMAT.format(new Date()));
        }

        stringBuilder
                .append(" [")
                .append(Thread.currentThread().getName())
                .append("] ")
                .append(message);

        filePrintStream.println(stringBuilder);
        System.out.println(stringBuilder);
    }
}
