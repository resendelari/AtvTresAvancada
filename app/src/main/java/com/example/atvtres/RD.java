package com.example.atvtres;

public class RD {
    public static void main(String[] args) {
        // Dados fornecidos
        double[] yArray = {26240.0, 28560.3, 33030.8, 23464.2, 24969.4, 30549.5, 31233.0};
        double[] varianciasArray = {23569.2, 29953.8, 29654.6, 21072.2, 22435.1, 35241.1, 25539.8};

        // Criação da matriz y
        double[][] yMatrix = new double[7][1];
        for (int i = 0; i < 7; i++) {
            yMatrix[i][0] = yArray[i];
        }

        // Criação da matriz de variância V
        double[][] V = new double[7][7];
        for (int i = 0; i < 7; i++) {
            V[i][i] = varianciasArray[i];
        }

        // Criação da matriz A
        double[][] A = {
                {-1, 0, 0, 0, 0, 0, 0},
                {1, -1, 0, 0, 0, 0, 0},
                {0, 1, -1, 0, 0, 0, 0},
                {0, 0, 1, -1, 0, 0, 0},
                {0, 0, 0, 1, -1, 0, 0},
                {0, 0, 0, 0, 1, -1, 0},
                {0, 0, 0, 0, 0, 1, 0}
        };

        // Calcular A @ V @ A.T
        double[][] A_V_A_T = multiplyMatrices(A, multiplyMatrices(V, transpose(A)));

        // Calcular a inversa de A_V_A_T
        double[][] A_V_A_T_inv = invertMatrix(A_V_A_T);

        // Calcular yhat
        double[][] yhat = subtractMatrices(yMatrix, multiplyMatrices(A_V_A_T_inv, multiplyMatrices(A, yMatrix)));

        // Exibir os valores reconciliados
        System.out.println("Valores reconciliados:");
        for (int i = 0; i < yhat.length; i++) {
            System.out.println(yhat[i][0]);
        }
    }

    // Função para multiplicar duas matrizes
    public static double[][] multiplyMatrices(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Número de colunas da primeira matriz deve ser igual ao número de linhas da segunda matriz.");
        }

        double[][] result = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    // Função para transpor uma matriz
    public static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    // Função para inverter uma matriz (Usando o método de Gauss-Jordan)
    public static double[][] invertMatrix(double[][] matrix) {
        int n = matrix.length;
        double[][] augmentedMatrix = new double[n][2 * n];

        // Criação da matriz aumentada
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmentedMatrix[i][j] = matrix[i][j];
                augmentedMatrix[i][j + n] = (i == j) ? 1 : 0;
            }
        }

        // Aplicar o método de Gauss-Jordan
        for (int i = 0; i < n; i++) {
            // Encontrar o pivô
            double pivot = augmentedMatrix[i][i];
            for (int j = 0; j < 2 * n; j++) {
                augmentedMatrix[i][j] /= pivot;
            }
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmentedMatrix[k][i];
                    for (int j = 0; j < 2 * n; j++) {
                        augmentedMatrix[k][j] -= factor * augmentedMatrix[i][j];
                    }
                }
            }
        }

        // Extrair a matriz inversa
        double[][] inverse = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inverse[i][j] = augmentedMatrix[i][j + n];
            }
        }
        return inverse;
    }

    // Função para subtrair duas matrizes
    public static double[][] subtractMatrices(double[][] a, double[][] b) {
        int rows = a.length;
        int cols = a[0].length;

        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }
}
