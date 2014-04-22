// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

/**
 * Evaluates a mathematical expression.
 *
 * @author Paul King
 * @author Rob Nielsen
 */
public class Expression
{

    private Evaluator fEvaluator;
    /**
     * a private instance for the static evaluate method
     */
    private static Expression sInstance;

    /**
     * Constructs a new blank expression
     */
    public Expression() {
    }

    /**
     * Constructor for the Expression object
     *
     * @param eval the fEvaluator to use for unknown expressions
     */
    public Expression(Evaluator eval) {
        setEvaluator(eval);
    }

    /**
     * Sets the Evaluator attribute of the Expression object
     *
     * @param evaluator The new Evaluator value
     */
    public void setEvaluator(Evaluator evaluator) {
        this.fEvaluator = evaluator;
    }

    /**
     * Evaluates the expression string and returns the result (which is also available
     * with getValue()).  Any parse errors will throw a IllegalArgumentException
     *
     * @param exp the string to parse
     * @return the double value of the expression
     * @throws IllegalArgumentException
     */
    public double evaluate(String exp) {
        if (exp == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(exp);
        }
        catch (NumberFormatException e) {
            return evaluate(exp.toCharArray(), 0, exp.length());
        }
    }

    /**
     * Evaluates a string found in the expression which isn't an arithmetic
     * expression.  Calls out to the given fEvaluator.
     *
     * @param s the substring to evaluate
     * @return the double value of the evaluation
     */
    protected double evaluateString(String s) {
        if (fEvaluator != null) {
            return fEvaluator.evaluate(s.trim());
        }
        throw new IllegalArgumentException("Cannot parse: '" + s + "': No evaluator");
    }

    /**
     * Recursively evaluates the exp contained in the char array e between
     * start and end.
     *
     * @param e     the exp
     * @param origStart the start index (inclusive) to evaluate from
     * @param origEnd   the end index (exclusive) to evaluate to
     * @return the evaluated exp
     * @throws IllegalArgumentException if the exp can't be parsed.
     */
    private double evaluate(char[] e, int origStart, int origEnd) {
        int start = origStart;
        int end = origEnd;
        while (start < end && e[start] == ' ') {
            start++;
        }
        if (end == start) {
            return 0;
        }

        while (e[end - 1] == ' ') {
            end--;
        }

        boolean number = true;
        int bracket = 0;
        for (int add = 0; add < 2; add++) {
            for (int i = end - 1; i >= start; i--) {
                if (e[i] == ')') {
                    bracket++;
                } else if (e[i] == '(') {
                    bracket--;
                } else if (bracket == 0) {
                    if (add == 0) {
                        if (e[i] == '+') {
                            return evaluate(e, start, i) + evaluate(e, i + 1, end);
                        }
                        if (e[i] == '-') {
                            return evaluate(e, start, i) - evaluate(e, i + 1, end);
                        }
                    } else {
                        if (e[i] == '*') {
                            return evaluate(e, start, i) * evaluate(e, i + 1, end);
                        }
                        if (e[i] == '/') {
                            return evaluate(e, start, i) / evaluate(e, i + 1, end);
                        }
                        if (e[i] == '%') {
                            return evaluate(e, start, i) % evaluate(e, i + 1, end);
                        }
                    }
                }
                if ((e[i] < '0' || e[i] > '9') && e[i] != '.') {
                    number = false;
                }
            }
        }
        if (e[end - 1] == ')' && e[start] == '(') {
            start++;
            end--;
            if (end == start) {
                return 0;
            }
            return evaluate(e, start, end);
        }

        String s = new String(e, start, end - start);
        if (number) {
            return Double.parseDouble(s);
        }
        return evaluateString(s);
    }


    /**
     * Evaluates a string expression without requiring the creation of an instance.
     *
     * @param s the expression to evaluate
     * @return the double value of the expression
     */
    public static double evaluateExpression(String s) {
        return evaluateExpression(s, null);
    }

    /**
     * Evaluates a string expression without requiring the creation of an instance.
     *
     * @param s    the expression to evaluate
     * @param eval the fEvaluator to parse special values
     * @return the double value of the expression
     */
    public static double evaluateExpression(String s, Evaluator eval) {
        if (sInstance == null) {
            sInstance = new Expression(eval);
        } else {
            sInstance.setEvaluator(eval);
        }

        return sInstance.evaluate(s);
    }
}
