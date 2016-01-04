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

import java.util.ArrayList;

/**
 *
 *
 * @author
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

    static ArrayList<Double> x;
    static ArrayList<Double> temperature;
    static ArrayList<Double> kappa;

    static GraphPanel mainPanel;

    private static void setIC(final double step) {
        setDomain(step);
        setInitialVariables();
    }

    private static void setInitialVariables() {
        for (int i = 0; i < IMAX; i++) {
            boolean result = (x.get(i) < 0) ? left() : right();

            if (!result) throw new RuntimeException("error!");
        }
    }

    private static void setDomain(final double step) {
    
        x.add(xL);
        for (int i = 1; i < IMAX; i++) x.add(x.get(i-1) + step);

    }

    private static boolean left() {

        temperature.add(TL);
        kappa.add(kappaL);

        return true;

    }

    private static boolean right() {

        temperature.add(TR);
        kappa.add(kappaR);

        return true;

    }

    private static void setVariables() {

        x = new ArrayList<Double>(IMAX);
        temperature = new ArrayList<Double>(IMAX);
        kappa = new ArrayList<Double>(IMAX);

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

        ArrayList<Double> tmpTemperature = new ArrayList<Double>(IMAX);

        for (int i = 0; i < IMAX; i++) {

            double fluxPlus;
            double fluxMinus;

            if (i == 0) {

                double kappaPlus = 0.5 * (kappa.get(i) + kappa.get(i + 1));
                double kappaMinus = 0.5 * (kappa.get(i) + kappaL);

                fluxPlus = kappaPlus * (temperature.get(i + 1) - temperature.get(i)) / spacing;
                fluxMinus = kappaMinus * (temperature.get(i) - TL) / (spacing / 2);

            } else if (i == (IMAX - 1)) {

                double kappaPlus = 0.5 * (kappa.get(i) + kappaR);
                double kappaMinus = 0.5 * (kappa.get(i) + kappa.get(i - 1));

                fluxPlus = kappaPlus * (TR - temperature.get(i)) / (spacing / 2);
                fluxMinus = kappaMinus * (temperature.get(i) - temperature.get(i - 1)) / spacing;

            } else {

                double kappaPlus = 0.5 * (kappa.get(i) + kappa.get(i + 1));
                double kappaMinus = 0.5 * (kappa.get(i) + kappa.get(i - 1));

                fluxPlus = kappaPlus * (temperature.get(i + 1) - temperature.get(i)) / spacing;
                fluxMinus = kappaMinus * (temperature.get(i) - temperature.get(i - 1)) / spacing;

            }

            double tmp_val = temperature.get(i) + timeStep / spacing * (fluxPlus - fluxMinus);
            tmpTemperature.add(i, tmp_val);

        }

        temperature.removeAll(temperature);
        temperature.addAll(tmpTemperature);

    }

    private static double setTimeStep(final double spacing) {

        return 0.45 * Math.pow(spacing, 2.0) / maxKappa();
    }

    private static double maxKappa() {

        double max = kappa.get(0);

        for (Double val : kappa)
            max = (val > max) ? val : max;

        return max;

    }

    public static void main (String[] args) {

        final double dx = (xR - xL) / IMAX;

        setVariables();
        setIC(dx);

        mainPanel = new GraphPanel(temperature);

        temporalLoop(dx);
    }

}
