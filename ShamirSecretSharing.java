import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShamirSecretSharing {

    public static void main(String[] args) {
        // JSON-like input string
        String jsonInput = "{\n" +
                "    \"keys\": {\n" +
                "        \"n\": \"4\",\n" +
                "        \"k\": \"3\"\n" +
                "    },\n" +
                "    \"1\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"4\"\n" +
                "    },\n" +
                "    \"2\": {\n" +
                "        \"base\": \"2\",\n" +
                "        \"value\": \"111\"\n" +
                "    },\n" +
                "    \"3\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"12\"\n" +
                "    },\n" +
                "    \"6\": {\n" +
                "        \"base\": \"4\",\n" +
                "        \"value\": \"213\"\n" +
                "    }\n" +
                "}";

        // Parse JSON-like input
        ParsedData data = parseJson(jsonInput);

        int n = data.n;
        int k = data.k;

        List<Point> points = new ArrayList<>();
        for (String key : data.points.keySet()) {
            int x = Integer.parseInt(key);
            String yValue = data.points.get(key).get("value");
            int base = Integer.parseInt(data.points.get(key).get("base"));
            BigInteger y = new BigInteger(yValue, base);
            points.add(new Point(x, y));
        }

        // Use Lagrange interpolation to find the polynomial
        Polynomial polynomial = LagrangeInterpolation.interpolate(points);

        // Evaluate the polynomial at x=0 to find the constant term
        BigInteger constantTerm = polynomial.evaluate(BigInteger.ZERO);

        System.out.println("Constant term: " + constantTerm);
    }

    static class Point {
        int x;
        BigInteger y;

        public Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Polynomial {
        List<BigInteger> coefficients;

        Polynomial(List<BigInteger> coefficients) {
            this.coefficients = coefficients;
        }

        // Evaluate polynomial at x
        public BigInteger evaluate(BigInteger x) {
            BigInteger result = BigInteger.ZERO;
            BigInteger power = BigInteger.ONE;
            for (BigInteger coeff : coefficients) {
                result = result.add(coeff.multiply(power));
                power = power.multiply(x);
            }
            return result;
        }
    }

    static class LagrangeInterpolation {
        public static Polynomial interpolate(List<Point> points) {
            int n = points.size();
            List<BigInteger> coefficients = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                coefficients.add(BigInteger.ZERO);
            }

            for (int i = 0; i < n; i++) {
                Point pi = points.get(i);
                BigInteger xi = BigInteger.valueOf(pi.x);
                BigInteger yi = pi.y;

                // Compute Lagrange basis polynomial L_i(x)
                List<BigInteger> liCoefficients = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        Point pj = points.get(j);
                        BigInteger xj = BigInteger.valueOf(pj.x);
                        BigInteger denom = xi.subtract(xj);
                        BigInteger num = BigInteger.ZERO.subtract(xj);
                        BigInteger liCoeff = num.multiply(BigInteger.ONE.divide(denom));
                        liCoefficients.add(liCoeff);
                    }
                }

                Polynomial li = new Polynomial(liCoefficients);
                List<BigInteger> newCoefficients = new ArrayList<>();
                for (int k = 0; k < coefficients.size(); k++) {
                    newCoefficients.add(BigInteger.ZERO);
                }
                for (int k = 0; k < coefficients.size(); k++) {
                    newCoefficients.set(k, newCoefficients.get(k).add(yi.multiply(li.evaluate(BigInteger.ZERO))));
                }
                coefficients = newCoefficients;
            }

            return new Polynomial(coefficients);
        }
    }

    // Simple parser for the JSON-like format
    public static ParsedData parseJson(String json) {
        ParsedData data = new ParsedData();
        data.points = new HashMap<>();

        String[] lines = json.split("\n");
        boolean parsingKeys = false;
        boolean parsingPoint = false;
        String currentKey = null;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("\"n\":")) {
                data.n = Integer.parseInt(line.split(":")[1].trim().replaceAll("\"", "").replaceAll(",", ""));
            } else if (line.startsWith("\"k\":")) {
                data.k = Integer.parseInt(line.split(":")[1].trim().replaceAll("\"", "").replaceAll(",", ""));
            } else if (line.matches("\"\\d+\": \\{")) {
                parsingPoint = true;
                currentKey = line.split(":")[0].replaceAll("\"", "").trim();
                data.points.put(currentKey, new HashMap<>());
            } else if (parsingPoint) {
                if (line.startsWith("\"base\":")) {
                    String base = line.split(":")[1].trim().replaceAll("\"", "").replaceAll(",", "");
                    data.points.get(currentKey).put("base", base);
                } else if (line.startsWith("\"value\":")) {
                    String value = line.split(":")[1].trim().replaceAll("\"", "").replaceAll(",", "");
                    data.points.get(currentKey).put("value", value);
                    parsingPoint = false;
                }
            }
        }
        return data;
    }

    // Class to hold parsed data
    static class ParsedData {
        int n;
        int k;
        Map<String, Map<String, String>> points;
    }
}
