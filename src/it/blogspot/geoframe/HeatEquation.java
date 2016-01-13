/*
 * GNU GPL v3 License
 *
 * Copyright 2015 AboutHydrology (Riccardo Rigon)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.blogspot.geoframe;

/**
 *
 *
 * @author sidereus, francesco.serafin.3@gmail.com
 * @version 0.1
 * @date November 08, 2015
 * @copyright GNU Public License v3 AboutHydrology (Riccardo Rigon)
 */
public class HeatEquation {

    static final double xL = -1;
    static final double xR = +1;
    static final int IMAX = 100;
    static final int NMAX = 1000;
    static final double kappaL = 1;
    static final double kappaR = 0.1;
    static double time = 0;
    static final double tend = 1;
    static final double TL = 20;
    static final double TR = 0;

    static Double[] x;
    static Double[] temperature;
    static Double[] tmpTemperature;
    static Double[] kappa;

    static GraphPanel mainPanel;

    private static void setIC(final double step) {
        setDomain(step);
        variablesInitialization();
    }

    private static void setDomain(final double step) {
    
        x[0] = xL;
        for (int i = 1; i < IMAX; i++) x[i] = x[i-1] + step;

    }

    private static void variablesInitialization() {
        for (int i = 0; i < IMAX; i++) {
            boolean result = (x[i] < 0) ? left(i) : right(i);

            if (!result) throw new RuntimeException("error!");
        }
    }

    private static boolean left(final int index) {

        temperature[index] = TL;
        kappa[index] = kappaL;

        return true;

    }

    private static boolean right(final int index) {

        temperature[index] = TR;
        kappa[index] = kappaR;

        return true;

    }

    private static void memoryAllocation() {

        x = new Double[IMAX];
        temperature = new Double[IMAX];
        tmpTemperature = new Double[IMAX];
        kappa = new Double[IMAX];

    }

    private static void temporalLoop(final double spacing) {

        for (int i = 0; i < NMAX; i++) {

            double dt = setTimeStep(spacing);
            if (time + dt > tend) dt = tend - time;
            else if (time >= tend) break;

            computation(spacing, dt);

            mainPanel.setScores(temperature);

            time = time + dt;

        }

    }

    private static void computation(final double spacing, final double timeStep) {

        double fluxPlus;
        double fluxMinus;
        double kappaPlus;
        double kappaMinus;
        double tmp_val;

        for (int i = 0; i < IMAX; i++) {

            if (i == 0) {

                kappaPlus = 0.5 * (kappa[i] + kappa[i + 1]);
                kappaMinus = 0.5 * (kappa[i] + kappaL);

                fluxPlus = kappaPlus * (temperature[i + 1] - temperature[i]) / spacing;
                fluxMinus = kappaMinus * (temperature[i] - TL) / (spacing / 2);

            } else if (i == (IMAX - 1)) {

                kappaPlus = 0.5 * (kappa[i] + kappaR);
                kappaMinus = 0.5 * (kappa[i] + kappa[i - 1]);

                fluxPlus = kappaPlus * (TR - temperature[i]) / (spacing / 2);
                fluxMinus = kappaMinus * (temperature[i] - temperature[i - 1]) / spacing;

            } else {

                kappaPlus = 0.5 * (kappa[i] + kappa[i + 1]);
                kappaMinus = 0.5 * (kappa[i] + kappa[i - 1]);

                fluxPlus = kappaPlus * (temperature[i + 1] - temperature[i]) / spacing;
                fluxMinus = kappaMinus * (temperature[i] - temperature[i - 1]) / spacing;

            }

            tmp_val = temperature[i] + timeStep / spacing * (fluxPlus - fluxMinus);
            tmpTemperature[i] = tmp_val;

        }

        temperature = tmpTemperature;

    }

    private static double setTimeStep(final double spacing) {
        return 0.45 * Math.pow(spacing, 2.0) / maxKappa();
    }

    private static double maxKappa() {

        double max = kappa[0];

        for (Double val : kappa)
            max = (val > max) ? val : max;

        return max;

    }

    public static void main (String[] args) {

        final double dx = (xR - xL) / IMAX;

        memoryAllocation();
        setIC(dx);

        mainPanel = new GraphPanel(temperature);

        temporalLoop(dx);
    }

}
