import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CurrencyConverterGUI {

    private static final String API_KEY = "590a9e6628445541f36a3b1a";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    
    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR", "MXN", "COP", "BRL", "ARS", "CLP", "KRW"};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Coinvertor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

         
            JPanel welcomePanel = new JPanel();
            welcomePanel.add(new JLabel("¡Bienvenido al Coinvertor!"));
            frame.add(welcomePanel, BorderLayout.NORTH);

         
            JPanel conversionPanel = new JPanel(new GridLayout(4, 2));
            
            JLabel baseCurrencyLabel = new JLabel("Moneda base:");
            JComboBox<String> baseCurrencyComboBox = new JComboBox<>(CURRENCIES);
            JLabel targetCurrencyLabel = new JLabel("Moneda a convertir:");
            JComboBox<String> targetCurrencyComboBox = new JComboBox<>(CURRENCIES);
            JLabel amountLabel = new JLabel("Monto:");
            JTextField amountField = new JTextField();
            JButton convertButton = new JButton("Convertir");
            JLabel resultLabel = new JLabel("Resultado:");

            conversionPanel.add(baseCurrencyLabel);
            conversionPanel.add(baseCurrencyComboBox);
            conversionPanel.add(targetCurrencyLabel);
            conversionPanel.add(targetCurrencyComboBox);
            conversionPanel.add(amountLabel);
            conversionPanel.add(amountField);
            conversionPanel.add(convertButton);
            conversionPanel.add(resultLabel);
            frame.add(conversionPanel, BorderLayout.CENTER);

            convertButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String baseCurrency = (String) baseCurrencyComboBox.getSelectedItem();
                    String targetCurrency = (String) targetCurrencyComboBox.getSelectedItem();
                    double amount;
                    try {
                        amount = Double.parseDouble(amountField.getText());
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Por favor, ingresa un monto válido.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double exchangeRate = getExchangeRate(baseCurrency, targetCurrency);
                    if (exchangeRate != -1) {
                        double convertedAmount = amount * exchangeRate;
                        resultLabel.setText(String.format("%.2f %s = %.2f %s", amount, baseCurrency, convertedAmount, targetCurrency));
                    } else {
                        resultLabel.setText("No se pudo obtener la tasa de cambio.");
                    }
                }
            });

            frame.setVisible(true);
        });
    }

    public static double getExchangeRate(String baseCurrency, String targetCurrency) {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL(API_URL + baseCurrency);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != 200) {
                System.out.println("Error: No se pudo conectar a la API. Código de estado HTTP: " + status);
                return -1;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();

            String jsonResponse = content.toString();
            Map<String, Double> rates = parseRatesFromJson(jsonResponse);
            return rates.getOrDefault(targetCurrency, -1.0);
        } catch (Exception e) {
            System.out.println("Error al obtener la tasa de cambio: " + e.getMessage());
            return -1;
        }
    }

    private static Map<String, Double> parseRatesFromJson(String jsonResponse) {
        Map<String, Double> rates = new HashMap<>();
        try {
            int ratesIndex = jsonResponse.indexOf("\"conversion_rates\":{");
            if (ratesIndex == -1) {
                return rates;
            }
            int start = ratesIndex + "\"conversion_rates\":{".length();
            int end = jsonResponse.indexOf('}', start);
            String ratesString = jsonResponse.substring(start, end);

            String[] pairs = ratesString.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String currency = keyValue[0].replace("\"", "").trim();
                    double rate = Double.parseDouble(keyValue[1].trim());
                    rates.put(currency, rate);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al procesar el JSON: " + e.getMessage());
        }
        return rates;
    }
}

