package com.example.atvtres;

public class CombinedTrajectoriesAnalysis {

    public static void main(String[] args) {
        // Dados fictícios para todas as trajetórias combinadas
        int numberOfTrajectories = 3;
        int numberOfMeasurementsPerTrajectory = 6;
        int numberOfReadingsPerMeasurement = 10;
        int totalMeasurements = numberOfTrajectories * numberOfMeasurementsPerTrajectory * numberOfReadingsPerMeasurement;

        double[] rawMeasurements = new double[totalMeasurements];
        double[] standardDeviations = new double[totalMeasurements];
        double[][] incidenceMatrix = new double[totalMeasurements][totalMeasurements];

        // Preenchendo dados fictícios
        // rawMeasurements e standardDeviations devem ser preenchidos com as leituras reais

        // Criar matriz de incidência
        // Exemplo simplificado - você deve construir a matriz com base na configuração do sistema
        for (int i = 0; i < totalMeasurements; i++) {
            incidenceMatrix[i][i] = 1; // Simples identidade para exemplo
        }

        // Inicializar a classe de reconciliação
        Reconciliation reconciliation = new Reconciliation(rawMeasurements, standardDeviations, incidenceMatrix);

        // Obter o vetor reconciliado
        double[] reconciledFlow = reconciliation.getReconciledFlow();

        // Imprimir resultados
        System.out.println("Reconciled Measurements for Combined Trajectories:");
        for (double value : reconciledFlow) {
            System.out.println(value);
        }
    }
}
