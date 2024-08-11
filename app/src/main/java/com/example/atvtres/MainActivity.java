package com.example.atvtres;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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
    private static final int DELAY_MS = 100; // Intervalo entre atualizações (simulação de carro)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //quando o mapa está pronto
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Adicionar marcadores coloridos (azul - inicial; verde - final)
        mMap.addMarker(new MarkerOptions()
                .position(pontoInicial)
                .title("Ponto Inicial")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Marcador azul

        mMap.addMarker(new MarkerOptions()
                .position(pontoFinal)
                .title("Ponto Final")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Marcador verde

        // Move a câmera para o ponto inicial
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pontoInicial, 15));

        getRoutes();
    }

    //requisição para a API de Direções do Google para buscar rotas entre os pontos inicial e final
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
                                addFlowMeters(polylinePoints);

                                // Iniciar simulação para cada rota
                                startCarSimulation(polylinePoints, i);
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


    //adiciona medidores de fluxo
    private void addFlowMeters(List<LatLng> points) {
        if (points.size() < 6) return; // Garantir que haja pelo menos 6 medidores

        // Adiciona marcadores para os medidores de fluxo
        int interval = points.size() / 6;
        for (int i = 0; i < 6; i++) {
            LatLng position = points.get(i * interval);
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Medidor " + (i + 1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // Marcador vermelho
        }
    }

    //simulação de movimento de um carro ao longo do percurso
    private void startCarSimulation(final List<LatLng> routePoints, int routeIndex) {
        if (routePoints.isEmpty()) return;

        final LatLng carStart = routePoints.get(0);
        final LatLng carEnd = routePoints.get(routePoints.size() - 1);

        // Diferentes ícones para cada carro (se necessário, crie recursos diferentes)
        int[] carIcons = {R.drawable.car_icon, R.drawable.car_icon, R.drawable.car_icon};

        // Criar e adicionar o marcador do carro com ícone redimensionado
        final MarkerOptions carMarkerOptions = new MarkerOptions()
                .position(carStart)
                .title("Carro " + (routeIndex + 1))
                .icon(resizeIcon(carIcons[routeIndex % carIcons.length], 200, 100));


        final com.google.android.gms.maps.model.Marker carMarker = mMap.addMarker(carMarkerOptions);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int currentIndex = 0;

            @Override
            public void run() {
                if (currentIndex < routePoints.size() - 1) {
                    LatLng nextPoint = routePoints.get(currentIndex + 1);
                    carMarker.setPosition(nextPoint);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(nextPoint));
                    currentIndex++;
                    handler.postDelayed(this, DELAY_MS);
                } else {
                    // Reposicionar o carro no ponto final
                    carMarker.setPosition(carEnd);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(carEnd));
                }
            }
        }, DELAY_MS);
    }


    //decodifica a polilinha (string codificada) fornecida pela API do Google, convertendo-a em uma lista de coordenadas LatLng para ser utilizada na exibição da rota
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
            LatLng p = new LatLng((((lat * 1E-5))), (((lng * 1E-5))));
            polyline.add(p);
        }

        return polyline;
    }

    // Função para redimensionar o ícone do carro
    private BitmapDescriptor resizeIcon(int resourceId, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }
}