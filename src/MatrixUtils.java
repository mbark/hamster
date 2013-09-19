
public class MatrixUtils {
	
	public static char[][] matrixCopy (char[][] matrix) {
		char[][] copy = new char[matrix.length][];
		for (int row = 0; row < copy.length; row++)
			copy[row] = matrix[row].clone();
		return copy;
	}
}
