/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package truthtablegenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates tables from logical expressions
 */
public class CompactTableGenerator {
	private List<List <String>> compactTable = new ArrayList<>();
	private List<Integer> displayOrder = new ArrayList<>();
	
	/**
	 * Makes the full fullTable row by row
	 */
	private void makeTable() {
		int tableSize = (int) Math.pow(2 ,Expression.getVariableCount());
		compactTable.clear();
		displayOrder.clear();
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
		
		
		for(int i = 0; i < tableSize; i++) {
			calcRow(i, compactTableString);
		}
		//if the expression is a single character, display will be empty so add in a 0 to display the single character.
		if (displayOrder.size() == 0) { 
			displayOrder.add(0); 
		}
		System.out.println(displayOrder);
		System.out.print('[');
		for (int i = 0; i < compactTableString.length(); i ++) {
			System.out.print(i + ", ");
		}
		System.out.println();
		for (List<String> row : compactTable) {
			System.out.println(row);
		}
	}
	
	/**
	 * Prepares the String expression to be calculated.
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
		int expLength = expression.length();
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
		}
		
		int currentVar = binaryStep.length();
		
		calcStep(expression, 0, step + 1);
		//fullTable.add(stringResults);
	}
	
	/**
	 * Calculates a single step. Recursively calculates parenthetical steps
	 * @param startPoint the point to start the search. used for recursive calls. initial call should be 0.
	 * @param step the step to calculate
	 * @return string (used for recursion. not needed for original call.
	 */
	public String calcStep(String step, int s, int row) {
		int endPoint = 0;
		for (int i = 0; i < step.length(); i ++) {
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
				step =  step.substring(0, i) + parStep + step.substring(i + skip);
			} 
		}
		String expression = Expression.getCompactExpression();
		endPoint = step.length();
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '~') {
				String result = Integer.toString(BinaryMath.not(Character.getNumericValue(step.charAt(i + 1))));
				step = step.substring(0, i) + result + result + step.substring(i + 2);
				compactTable.get(row).set(i + s, result);
				
				//if the RHS is a letter (not a '(' ) then add it to the order
				if (Character.isLetter(expression.charAt(i + 1 + s))) {
					addToOrder(i + 1 + s);
				}
				//then add the negation regardless
				addToOrder(i + s);
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '*') {
				String result = Integer.toString(BinaryMath.and(Character.getNumericValue(step.charAt(i - 1)), 
						Character.getNumericValue(step.charAt(i + 1))));
				step = step.substring(0, i - 1) +  result + result + result + step.substring(i + 2);
				compactTable.get(row).set(i + s, result);
				
				//if the LHS is a letter (not a ')' ) then add it to the order
				if (Character.isLetter(expression.charAt(i - 1 + s))) {
					addToOrder(i - 1 + s);
				}
				//if the RHS is a letter (not a '(' ) then add it to the order
				if (Character.isLetter(expression.charAt(i + 1 + s))) {
					addToOrder(i + 1 + s);
				}
				//then add the AND regardless
				addToOrder(i + s);
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '+') {
				String result = Integer.toString(BinaryMath.or(Character.getNumericValue(step.charAt(i - 1)), 
						Character.getNumericValue(step.charAt(i + 1))));
				step = step.substring(0, i - 1) +  result + result + result + step.substring(i + 2);
				compactTable.get(row).set(i + s, result);
				//if the LHS is a letter (not a ')' ) then add it to the order
				if (Character.isLetter(expression.charAt(i - 1 + s))) {
					System.out.println(expression.charAt(i - 1 + s) + " at " + (i - 1 + s));
					addToOrder(i - 1 + s);
				}
				//if the RHS is a letter (not a '(' ) then add it to the order
				if (Character.isLetter(expression.charAt(i + 1 + s))) {
					
					System.out.println(expression.charAt(i +1 + s) + " at " + (i + 1 + s));
					addToOrder(i + 1 + s);
				}
				//then add the OR regardless
				System.out.println("or at " + ( i + s));
				addToOrder(i + s);
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '>') {
				String result = Integer.toString(BinaryMath.implies(Character.getNumericValue(step.charAt(i - 1)), 
						Character.getNumericValue(step.charAt(i + 1))));
				step = step.substring(0, i - 1) +  result + result + result + step.substring(i + 2);
				compactTable.get(row).set(i + s, result);
				//if the LHS is a letter (not a ')' ) then add it to the order
				if (Character.isLetter(expression.charAt(i - 1 + s))) {
					addToOrder(i - 1 + s);
				}
				//if the RHS is a letter (not a '(' ) then add it to the order
				if (Character.isLetter(expression.charAt(i + 1 + s))) {
					addToOrder(i + 1 + s);
				}
				//then add the IMP regardless
				addToOrder(i + s);
			}
		}
		for (int i = 0; i < endPoint; i++) {
			if (step.charAt(i) == '<') {
				String result = Integer.toString(BinaryMath.iff(Character.getNumericValue(step.charAt(i - 1)), 
						Character.getNumericValue(step.charAt(i + 1))));
				step = step.substring(0, i - 1) +  result + result + result + step.substring(i + 2);
				compactTable.get(row).set(i + s, result);
				//if the LHS is a letter (not a ')' ) then add it to the order
				if (Character.isLetter(expression.charAt(i - 1 + s))) {
					addToOrder(i - 1 + s);
				}
				//if the RHS is a letter (not a '(' ) then add it to the order
				if (Character.isLetter(expression.charAt(i + 1 + s))) {
					addToOrder(i + 1 + s);
				}
				//then add the IFF regardless
				addToOrder(i + s);
			}
		}
		return step;
	}
	
	/**
	 * checks if a position is already in displayOrder. if it isnt, it is the next step so it adds it to the end of the List displayOrder
	 * @param position 
	 */
	public void addToOrder(int position) {
		for (Integer i : displayOrder) {
			if (i == position) {
				return;
			}
		}
		displayOrder.add(position);
	}
	
	public List<Integer> getDisplayOrder() {
		return displayOrder;
	}
	/**
	 * Generates the Compact Table and then returns it
	 * The CompactTable is a two dimensional List, or a list of lists broken up by ROWS of elements
	 * the first row is the expression, broken down to individual characters,
	 * the rest of the rows are the results one line at a time.
	 * @return fullTable
	 */
	public List<List<String>> getTable() {
		makeTable();
		return compactTable;
	}
}
