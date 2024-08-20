package com.example.atvtres;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.ejml.data.Matrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String API_KEY = "AIzaSyD3v1nf8wDhhV0QYb68lG5NEtLxFwBZbh4";
    private final LatLng pontoInicial = new LatLng(-19.9356, -43.9299); // Radix BH
    private final LatLng pontoFinal = new LatLng(-19.9298749597142, -44.013890204877576); // Arena MRV
    private List<LatLng> routePoints = new ArrayList<>();
    private Handler handler = new Handler();
    private int currentIndex = 0;
    private static final int DELAY_MS = 50; // Intervalo entre atualizações (simulação de carro)

    // Lista com as posições dos medidores de fluxo
    private List<LatLng> flowMeterPositions = new ArrayList<>();
    // Lista para armazenar os tempos de passagem nos medidores de fluxo
    private List<Long> flowMeterTimes = new ArrayList<>();
    // Lista de listas para armazenar os medidores de fluxo de cada rota
    private List<List<LatLng>> flowMeterPositionsByRoute = new ArrayList<>();

    // Velocidades simuladas (em metros por segundo)
    private final double[] speeds = {5.0, 10.0, 15.0}; // Exemplo: 5 m/s, 10 m/s, 15 m/s

    // Lista para armazenar os tempos de deslocamento em diferentes passagens
    private List<List<Long>> trajectoryTimes = new ArrayList<>();
    // Declarar o contador de simulações como variável de instância
    private int simulationsInProgress;
    // Declaração do measuredData, assumindo 3 trajetórias, 10 execuções, 6 medidores de fluxo
    double[][][] measuredData = new double[3][10][6];
    private TextView reconciledDataTextView1;
    private TextView reconciledDataTextView2;
    private TextView reconciledDataTextView3;
    private LinearLayout dataContainer;
    private List<TextView> trajectoryTextViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar o LinearLayout que vai conter os dados
        dataContainer = findViewById(R.id.dataContainer);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        simulationsInProgress = 10;

        for (int i = 0; i < 3; i++) {
            trajectoryTimes.add(new ArrayList<>());
        }

        // Inicializar listas de tempos de medição para cada trajetória
        for (int i = 0; i < 3; i++) {
            trajectoryTimes.add(new ArrayList<>());
        }

        // Adicionar marcadores coloridos (azul - inicial; verde - final)
        mMap.addMarker(new MarkerOptions()
                .position(pontoInicial)
                .title("Ponto Inicial")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Marcador azul

        mMap.addMarker(new MarkerOptions()
                .position(pontoFinal)
                .title("Ponto Final")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Marcador verde

        // Ajusta o zoom para incluir tanto o ponto inicial quanto o ponto final
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pontoInicial);
        builder.include(pontoFinal);
        LatLngBounds bounds = builder.build();
        int padding = 100; // Padding em pixels
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        getRoutes();
    }

    private void getRoutes() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + pontoInicial.latitude + "," + pontoInicial.longitude
                + "&destination=" + pontoFinal.latitude + "," + pontoFinal.longitude
                + "&alternatives=true&key=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray routes = response.getJSONArray("routes");
                            int routeCount = Math.min(routes.length(), 3);
                            int[] colors = {Color.BLUE, Color.GREEN, Color.RED}; // Defina as cores aqui

                            for (int i = 0; i < routeCount; i++) {
                                JSONObject route = routes.getJSONObject(i);
                                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                                String points = overviewPolyline.getString("points");
                                List<LatLng> polylinePoints = decodePolyline(points);

                                // Atribuir uma cor diferente para cada rota
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .addAll(polylinePoints)
                                        .color(colors[i % colors.length]) // Cor da linha
                                        .width(5);
                                mMap.addPolyline(polylineOptions);

                                // Adicionar medidores de fluxo ao longo da trajetória
                                addFlowMeters(polylinePoints, i);

                                // Simular 10 passagens para a rota
                                for (int j = 0; j < 10; j++) {
                                    startCarSimulation(polylinePoints, i);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.add(request);
    }

    private void addFlowMeters(List<LatLng> points, int routeIndex) {
        if (points.size() < 6) return; // Garantir que haja pelo menos 6 medidores

        // Criar uma nova lista para os medidores desta rota
        List<LatLng> flowMetersForRoute = new ArrayList<>();

        // Adiciona marcadores para os medidores de fluxo
        int interval = points.size() / 6;
        for (int i = 0; i < 6; i++) {
            LatLng position = points.get(i * interval);

            // Adicionar a posição à lista de medidores de fluxo desta rota
            flowMetersForRoute.add(position);

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Medidor " + (i + 1) + " - Rota " + (routeIndex + 1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // Marcador vermelho
        }

        // Adicionar a lista de medidores desta rota à lista geral
        flowMeterPositionsByRoute.add(flowMetersForRoute);
    }

    //simulação de movimento de um carro ao longo do percurso
    private void startCarSimulation(final List<LatLng> routePoints, int routeIndex) {
        if (routePoints.isEmpty()) return;

        // Criar e adicionar o marcador do carro com ícone redimensionado
        int[] carIcons = {R.drawable.car_icon, R.drawable.car_icon, R.drawable.car_icon};
        final MarkerOptions carMarkerOptions = new MarkerOptions()
                .position(routePoints.get(0))
                .title("Carro " + (routeIndex + 1))
                .icon(resizeIcon(carIcons[routeIndex % carIcons.length], 200, 100));
        final com.google.android.gms.maps.model.Marker carMarker = mMap.addMarker(carMarkerOptions);

        // Inicializar handler para a simulação de movimento
        final Handler handler = new Handler();
        final int distancePerUpdate = 5; // Número de pontos a avançar por atualização
        final long delay = 500; // Intervalo de tempo entre atualizações em milissegundos

        handler.postDelayed(new Runnable() {
            int currentIndex = 0;
            long lastTime = System.currentTimeMillis();

            @Override
            public void run() {
                if (currentIndex < routePoints.size() - 1) {
                    // Avançar múltiplos pontos se necessário
                    for (int i = 0; i < distancePerUpdate && currentIndex < routePoints.size() - 1; i++) {
                        LatLng nextPoint = routePoints.get(currentIndex + 1);
                        LatLng currentPoint = routePoints.get(currentIndex);
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - lastTime;

                        // Armazenar o tempo de deslocamento
                        if (currentIndex > 0) {
                            trajectoryTimes.get(routeIndex).add(elapsedTime);
                        }

                        // Atualizar a posição do carro no mapa
                        carMarker.setPosition(nextPoint);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(nextPoint));

                        currentIndex++;
                        lastTime = currentTime;
                    }
                    // Continuar com o próximo update
                    handler.postDelayed(this, delay);
                } else {
                    // A rota terminou, retornar o carro ao ponto inicial
                    carMarker.setPosition(routePoints.get(0));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(routePoints.get(0)));

                    // Armazenar o tempo final da trajetória
                    trajectoryTimes.get(routeIndex).add(System.currentTimeMillis() - lastTime);

                    // Decrementar o contador de simulações em progresso
                    simulationsInProgress--;

                    // Verificar se todas as simulações foram concluídas
                    if (simulationsInProgress == 0) {
                        populateMeasuredData();
                        evaluateIsolatedTrajectories(measuredData);
                    }
                }
            }
        }, delay);
    }

    // Função para popular o measuredData com os tempos armazenados no trajectoryTimes
    private void populateMeasuredData() {
        for (int routeIndex = 0; routeIndex < 3; routeIndex++) {
            List<Long> times = trajectoryTimes.get(routeIndex);
            for (int simIndex = 0; simIndex < 10; simIndex++) {
                for (int flowMeterIndex = 0; flowMeterIndex < 6; flowMeterIndex++) {
                    measuredData[routeIndex][simIndex][flowMeterIndex] = times.get(simIndex * 6 + flowMeterIndex);
                }
            }
        }
    }

    // Função para avaliar cada trajetória isoladamente usando os dados em measuredData
    private void evaluateIsolatedTrajectories(double[][][] measuredData) {
        for (int trajectoryIndex = 0; trajectoryIndex < 3; trajectoryIndex++) {
            double[][] trajectoryTimes = measuredData[trajectoryIndex];
            double[] mean = new double[6];
            double[] stdDev = new double[6];
            double[] bias = new double[6];
            double[] precision = new double[6];
            double[] uncertainty = new double[6];

            // Calcular média, desvio padrão e outras estatísticas para os tempos de deslocamento
            for (int flowMeterIndex = 0; flowMeterIndex < 6; flowMeterIndex++) {
                double sum = 0;
                double sumSq = 0;
                for (int execution = 0; execution < 10; execution++) {
                    double time = trajectoryTimes[execution][flowMeterIndex];
                    sum += time;
                    sumSq += time * time;
                }
                mean[flowMeterIndex] = sum / 10;
                stdDev[flowMeterIndex] = Math.sqrt((sumSq / 10) - (mean[flowMeterIndex] * mean[flowMeterIndex]));

                // Calcular bias, precisão e incerteza para os tempos de deslocamento
                bias[flowMeterIndex] = calculateBias(trajectoryTimes, flowMeterIndex);
                precision[flowMeterIndex] = calculatePrecision(trajectoryTimes, flowMeterIndex);
                uncertainty[flowMeterIndex] = calculateUncertainty(trajectoryTimes, flowMeterIndex);
            }

            // Aplicar Reconciliação de Dados para os tempos de deslocamento em cada trajetória
            double[] reconciledTimes = applyDataReconciliation(mean, stdDev, bias, precision, uncertainty);

            // Armazenar ou exibir os dados reconciliados dos tempos
            storeReconciledData(trajectoryIndex, reconciledTimes);
        }
    }

    private double calculateBias(double[][] data, int flowMeterIndex) {
        double sum = 0;
        int numReadings = data.length;
        for (int i = 0; i < numReadings; i++) {
            sum += data[i][flowMeterIndex];
        }
        double mean = sum / numReadings;
        return mean;
    }


    private double calculatePrecision(double[][] data, int flowMeterIndex) {
        double mean = calculateBias(data, flowMeterIndex); // Aqui, o bias é usado como a média
        double sumSquaredDiffs = 0;
        int numReadings = data.length;
        for (int i = 0; i < numReadings; i++) {
            double diff = data[i][flowMeterIndex] - mean;
            sumSquaredDiffs += diff * diff;
        }
        double variance = sumSquaredDiffs / numReadings;
        double stdDev = Math.sqrt(variance);
        // Precisão é inversamente proporcional ao desvio padrão
        return 1 / stdDev;
    }


    private double calculateUncertainty(double[][] data, int flowMeterIndex) {
        double sum = 0;
        double sumSquared = 0;
        int numReadings = data.length;
        for (int i = 0; i < numReadings; i++) {
            double value = data[i][flowMeterIndex];
            sum += value;
            sumSquared += value * value;
        }
        double mean = sum / numReadings;
        double variance = (sumSquared / numReadings) - (mean * mean);
        return Math.sqrt(variance);
    }


    private double[] applyDataReconciliation(double[] mean, double[] stdDev, double[] bias, double[] precision, double[] uncertainty) {
        int length = mean.length;
        double[] reconciledData = new double[length];
        for (int i = 0; i < length; i++) {
            // Exemplo simples: ajusta a média com bias e aplica a precisão e incerteza
            reconciledData[i] = mean[i] + bias[i];
            // Ajuste com precisão e incerteza conforme necessário
        }
        return reconciledData;
    }


    private void storeReconciledData(int trajectoryIndex, double[] reconciledData) {
        // Criar um StringBuilder para armazenar os dados
        StringBuilder sb = new StringBuilder();
        sb.append("Trajetória ").append(trajectoryIndex).append("\n");

        // Adicionar os dados reconciliados ao StringBuilder
        for (int i = 0; i < reconciledData.length; i++) {
            sb.append("Medidor de Fluxo ").append(i).append(": ").append(reconciledData[i]).append("\n");
        }

        // Adicionar os dados à interface
        TextView trajectoryTextView = new TextView(this);
        trajectoryTextView.setText(sb.toString());
        trajectoryTextView.setTextSize(16);
        trajectoryTextView.setPadding(8, 8, 8, 8);
        trajectoryTextView.setBackgroundColor(Color.LTGRAY);
        trajectoryTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Adicionar o TextView ao LinearLayout
        dataContainer.addView(trajectoryTextView);
    }

    private BitmapDescriptor resizeIcon(int resourceId, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng(
                    (lat / 1E5),
                    (lng / 1E5)
            );
            polyline.add(p);
        }
        return polyline;
    }
}
