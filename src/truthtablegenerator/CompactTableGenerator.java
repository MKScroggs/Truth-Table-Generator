package truthtablegenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates tables from logical expressions
 * 
 * @author Bryce, McAllister, Tyler
 */
public class CompactTableGenerator {

	private List<List<String>> compactTable = new ArrayList<>();

	/**
	 * Makes the full fullTable row by row
	 */
	private void makeTable() {
		int tableSize = (int) Math.pow(2, Expression.getVariableCount());
		compactTable.clear();
		String compactTableString = new String(Expression.getCompactExpression());
		List<String> expressionRow = new ArrayList<>();
		List<String> emptyRow = new ArrayList<>();

		for (int i = 0; i < compactTableString.length(); i++) {
			// break the expression into component parts
			expressionRow.add(Character.toString(compactTableString.charAt(i)));
			// create a list of equal size to expression row of spaces. used to initalize the result rows so i can set elements
			// of the row without any shifting
			emptyRow.add(" ");
		}

		compactTable.add(expressionRow);

		for (int i = 0; i < tableSize; i++) {
			// need to make a new unque instance or all "empty rows" are all the same and change together whenever one is edited
			List<String> emptyRowUnique = new ArrayList<>(emptyRow);
			compactTable.add(emptyRowUnique);
		}

		for (int i = 0; i < tableSize; i++) {
			calcRow(i, compactTableString);
		}
	}

	/**
	 * Prepares the String expression to be calculated.
	 *
	 * @param step the current row being calculated
	 * @param expression the expression to be evaluated
	 */
	private void calcRow(int step, String expression) {
		int variableCount = Expression.getVariableCount();

		String binaryStep = Integer.toBinaryString(step);
		while (binaryStep.length() < variableCount) {
			binaryStep = "0" + binaryStep;
		}

		int binaryCounter = binaryStep.length() - 1;
		for (int i = expression.length() - 1; i >= 0; i--) {
			if (Character.isLetter(expression.charAt(i))) {
				Character c = expression.charAt(i);
				for (int j = i; j >= 0; j--) {
					if (c.equals(expression.charAt(j))) {
						compactTable.get(step + 1).set(j, Character.toString(binaryStep.charAt(binaryCounter)));
						expression = expression.substring(0, j) + binaryStep.charAt(binaryCounter) + expression.substring(j + 1);
					}
				}
				binaryCounter--;
			}
			if (expression.charAt(i) == '0') {
				compactTable.get(step + 1).set(i, "0");
			}
			if (expression.charAt(i) == '1') {
				compactTable.get(step + 1).set(i, "1");
			}
		}

		int currentVar = binaryStep.length();

		calcStep(expression, 0, step + 1);
		//fullTable.add(stringResults);
	}

	/**
	 * Calculates a single step. Recursively calculates parenthetical steps
	 *
	 * @param step the step to evaluate
	 * @param s where to skip to when working on parenthetical steps
	 * @param row which row we are working on
	 * @param step the step to calculate
	 * @return string (used for recursion. not needed for original call.
	 */
	public String calcStep(String step, int s, int row) {
		int endPoint = 0;
		for (int i = 0; i < step.length(); i++) {
			if (step.charAt(i) == '(') {
				int parCount = 1;
				int skip = 1;
				for (; parCount > 0; skip++) {
					if (step.charAt(skip + i) == '(') {
						parCount++;
					}
					if (step.charAt(skip + i) == ')') {
						parCount--;
					}
				}
				String parStep = calcStep(step.substring(i + 1, i + skip - 1), i + 1 + s, row);
				String result = Character.toString(parStep.charAt(0));
				parStep += result + result; //the expression is all the same number. 
				//add two more of that number to account for the parentheses
				step = step.substring(0, i) + parStep + step.substring(i + skip);
			}
		}
		// 
		endPoint = step.length();
		int lastStep = 0;
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '~') {
				String result = Integer.toString(BinaryMath.not(Character.getNumericValue(step.charAt(i + 1))));
				String rightResult = getRightStringResult(step, i, result.charAt(0));
				step = step.substring(0, i) + result + rightResult + step.substring(i + rightResult.length() + 1); //replace the operator and variable with their result twice.
				compactTable.get(row).set(i + s, result); // i+s is where you are in the whole step. set that spot in the row to the result
				lastStep = i;
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '*') {
				String result = Integer.toString(BinaryMath.and(Character.getNumericValue(step.charAt(i - 1)),
						Character.getNumericValue(step.charAt(i + 1))));
				String rightResult = getRightStringResult(step, i, result.charAt(0));
				String leftResult = getLeftStringResult(step, i, result.charAt(0));
				step = step.substring(0, i - leftResult.length()) + leftResult + result + rightResult + step.substring(i + rightResult.length() + 1);
				compactTable.get(row).set(i + s, result);
				lastStep = i;
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '+') {
				String result = Integer.toString(BinaryMath.or(Character.getNumericValue(step.charAt(i - 1)),
						Character.getNumericValue(step.charAt(i + 1))));
				String rightResult = getRightStringResult(step, i, result.charAt(0));
				String leftResult = getLeftStringResult(step, i, result.charAt(0));
				step = step.substring(0, i - leftResult.length()) + leftResult + result + rightResult + step.substring(i + rightResult.length() + 1);
				compactTable.get(row).set(i + s, result);
				lastStep = i;
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '>') {
				String result = Integer.toString(BinaryMath.implies(Character.getNumericValue(step.charAt(i - 1)),
						Character.getNumericValue(step.charAt(i + 1))));
				String rightResult = getRightStringResult(step, i, result.charAt(0));
				String leftResult = getLeftStringResult(step, i, result.charAt(0));
				step = step.substring(0, i - leftResult.length()) + leftResult + result + rightResult + step.substring(i + rightResult.length() + 1);
				compactTable.get(row).set(i + s, result);
				lastStep = i;
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '<') {
				String result = Integer.toString(BinaryMath.iff(Character.getNumericValue(step.charAt(i - 1)),
						Character.getNumericValue(step.charAt(i + 1))));
				String rightResult = getRightStringResult(step, i, result.charAt(0));
				String leftResult = getLeftStringResult(step, i, result.charAt(0));
				step = step.substring(0, i - leftResult.length()) + leftResult + result + rightResult + step.substring(i + rightResult.length() + 1);
				compactTable.get(row).set(i + s, result);
				lastStep = i;
			}
		}
		//create a string that is the same length but only the last steps result
		String replacedStep = "";
		for (int i = 0; i < step.length(); i++) {
			replacedStep += step.charAt(lastStep);
		}
		//return the new string
		return replacedStep;
	}
	
	private String getLeftStringResult(String step, int pos, char value) {
		String result = "";
		pos--; // the initial position is already taken care of
		for (; pos >= 0; pos--) {
			if (step.charAt(pos) != '0' && step.charAt(pos) != '1') {
				break;
			}
			result += value;
		}
		return result;
	}
	
	private String getRightStringResult(String step, int pos, char value) {
		String result = "";
		pos++; // the initial position is already taken care of
		int max = step.length();
		for (; pos < max; pos++) {
			if (step.charAt(pos) != '0' && step.charAt(pos) != '1') {
				break;
			}
			result += value;
		}
		return result;
	}

	/**
	 * Generates the Compact Table and then returns it The CompactTable is a two
	 * dimensional List, or a list of lists broken up by ROWS of elements the
	 * first row is the expression, broken down to individual characters, the rest
	 * of the rows are the results one line at a time.
	 *
	 * @return fullTable
	 */
	public List<List<String>> getTable() {
		makeTable();
		return compactTable;
	}
}
