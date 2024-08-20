package com.example.atvtres;

public class IsolatedTrajectoriesAnalysis {

    // Dados armazenados para cada trajetória
    private static double[][] rawMeasurements = new double[3][10];
    private static double[][] standardDeviations = new double[3][10];
    private static double[][][] incidenceMatrices = new double[3][10][6];

    public static void main(String[] args) {
        // Aqui você deve preencher os dados reais em vez de simular
        // rawMeasurements, standardDeviations e incidenceMatrices

        for (int i = 0; i < 3; i++) {
            // Analisar cada trajetória isoladamente
            double[] rawMeasurement = rawMeasurements[i];
            double[] standardDeviation = standardDeviations[i];
            double[][] incidenceMatrix = incidenceMatrices[i];

            // Inicializar a classe de reconciliação
            Reconciliation reconciliation = new Reconciliation(rawMeasurement, standardDeviation, incidenceMatrix);

            // Obter o vetor reconciliado
            double[] reconciledFlow = reconciliation.getReconciledFlow();

            // Imprimir resultados
            System.out.println("Reconciled Measurements for Trajectory " + (i + 1) + ":");
            for (double value : reconciledFlow) {
                System.out.println(value);
            }
            System.out.println();
        }
    }
}
