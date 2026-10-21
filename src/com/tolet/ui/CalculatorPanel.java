package com.tolet.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** EMI calculator, Rent-vs-Buy analyser and Affordability checker. */
public class CalculatorPanel extends JPanel implements Refreshable {

    /** @return {monthly EMI, total payable, total interest} */
    public static double[] emi(double principal, double ratePct, int years) {
        int n = years * 12;
        double r = ratePct / 12 / 100;
        double e = r == 0 ? principal / n : principal * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
        return new double[] { e, e * n, e * n - principal };
    }

    public CalculatorPanel() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        JPanel head = Ui.transparent(new GridLayout(0, 1));
        head.add(Ui.title("Smart Calculators")); head.add(Ui.muted("Plan your budget before you decide."));
        add(head, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("EMI Calculator", emiTab());
        tabs.addTab("Rent vs Buy", rentVsBuy());
        tabs.addTab("Affordability", afford());
        add(tabs, BorderLayout.CENTER);
    }

    @Override public void refresh() {}

    private static double num(JTextField f) {
        try { return Double.parseDouble(f.getText().replace(",", "").trim()); } catch (NumberFormatException e) { return -1; }
    }
    private static JTextField input(String v) { JTextField f = Ui.field(12); f.setText(v); return f; }

    private JComponent emiTab() {
        JTextField price = input("10000000"), down = input("20"), rate = input("9.5"), years = input("20");
        JLabel emi = Ui.label("-", Font.BOLD, 28, Ui.PRIMARY), l1 = Ui.label("", Font.PLAIN, 14, Ui.TEXT),
                l2 = Ui.label("", Font.PLAIN, 14, Ui.TEXT), l3 = Ui.label("", Font.PLAIN, 14, Ui.TEXT);
        JPanel chart = Ui.transparent(new BorderLayout());
        chart.setPreferredSize(new Dimension(380, 250));
        Runnable calc = () -> {
            double p = num(price), d = num(down), r = num(rate), y = num(years);
            if (p <= 0 || d < 0 || d >= 100 || r < 0 || y < 1) { emi.setText("Check inputs"); return; }
            double loan = p * (100 - d) / 100;
            double[] res = emi(loan, r, (int) y);
            emi.setText(Ui.money(res[0]) + " / month");
            l1.setText("Down payment: " + Ui.money(p - loan));
            l2.setText("Loan amount: " + Ui.money(loan) + "   |   Total interest: " + Ui.money(res[2]));
            l3.setText("Total payable: " + Ui.money(res[1]));
            Map<String, Double> m = new LinkedHashMap<>();
            m.put("Principal", loan); m.put("Interest", res[2]);
            chart.removeAll(); chart.add(new Charts.Pie("Loan breakdown", m)); chart.revalidate(); chart.repaint();
        };
        Ui.RButton go = Ui.primary("Calculate"); go.addActionListener(e -> calc.run());
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        Ui.row(form, 0, "Property price (BDT)", price); Ui.row(form, 1, "Down payment (%)", down);
        Ui.row(form, 2, "Interest rate (% p.a.)", rate); Ui.row(form, 3, "Loan term (years)", years);
        GridBagConstraints g = new GridBagConstraints(); g.gridy = 4; g.gridx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(8, 4, 8, 4);
        form.add(go, g);
        JPanel res = Ui.transparent(new GridLayout(0, 1, 0, 6));
        res.add(Ui.muted("Monthly EMI")); res.add(emi); res.add(l1); res.add(l2); res.add(l3);
        return twoCol(form, res, chart, calc);
    }

    private JComponent rentVsBuy() {
        JTextField price = input("9000000"), rent = input("40000"), inc = input("5"), rate = input("9.5"), down = input("20"),
                years = input("10"), app = input("6");
        JLabel verdict = Ui.label("-", Font.BOLD, 20, Ui.PRIMARY), a = Ui.label("", Font.PLAIN, 14, Ui.TEXT),
                b = Ui.label("", Font.PLAIN, 14, Ui.TEXT), c = Ui.label("", Font.PLAIN, 14, Ui.TEXT);
        Runnable calc = () -> {
            double p = num(price), r0 = num(rent), ri = num(inc), ir = num(rate), d = num(down), y = num(years), ap = num(app);
            if (p <= 0 || r0 <= 0 || ri < 0 || ir < 0 || d < 0 || d >= 100 || y < 1 || ap < -50) { verdict.setText("Check inputs"); return; }
            double loan = p * (100 - d) / 100, downAmt = p - loan;
            double[] e = emi(loan, ir, (int) y);
            double futureValue = p * Math.pow(1 + ap / 100, y);
            double buyNet = downAmt + e[1] - futureValue;        // cash out minus asset value at the end
            double rentTotal = 0, cur = r0;
            for (int i = 0; i < (int) y; i++) { rentTotal += cur * 12; cur *= 1 + ri / 100; }
            a.setText("BUY: down payment " + Ui.money(downAmt) + " + EMIs " + Ui.money(e[1]) + " = " + Ui.money(downAmt + e[1]));
            b.setText("Property value after " + (int) y + " years: " + Ui.money(futureValue) + "  ->  net cost " + Ui.money(buyNet));
            c.setText("RENT: total rent paid over " + (int) y + " years = " + Ui.money(rentTotal));
            double diff = Math.abs(buyNet - rentTotal);
            verdict.setText(buyNet < rentTotal ? "Buying saves about " + Ui.money(diff) : "Renting saves about " + Ui.money(diff));
        };
        Ui.RButton go = Ui.primary("Compare"); go.addActionListener(e -> calc.run());
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        Ui.row(form, 0, "Property price (BDT)", price); Ui.row(form, 1, "Equivalent monthly rent", rent);
        Ui.row(form, 2, "Yearly rent increase (%)", inc); Ui.row(form, 3, "Loan interest (% p.a.)", rate);
        Ui.row(form, 4, "Down payment (%)", down); Ui.row(form, 5, "Time horizon (years)", years);
        Ui.row(form, 6, "Yearly property appreciation (%)", app);
        GridBagConstraints g = new GridBagConstraints(); g.gridy = 7; g.gridx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(8, 4, 8, 4);
        form.add(go, g);
        JPanel res = Ui.transparent(new GridLayout(0, 1, 0, 8));
        res.add(Ui.muted("Verdict")); res.add(verdict); res.add(a); res.add(b); res.add(c);
        res.add(Ui.muted("Simplified estimate; ignores taxes, maintenance and investment returns."));
        return twoCol(form, res, null, calc);
    }

    private JComponent afford() {
        JTextField inc = input("100000"), debt = input("10000"), sav = input("2000000"), rate = input("9.5"), years = input("20");
        JLabel rentL = Ui.label("-", Font.BOLD, 22, Ui.PRIMARY), emiL = Ui.label("", Font.BOLD, 16, Ui.TEXT),
                loanL = Ui.label("", Font.BOLD, 16, Ui.TEXT), priceL = Ui.label("", Font.BOLD, 22, Ui.ACCENT.darker());
        Runnable calc = () -> {
            double i = num(inc), d = num(debt), s = num(sav), r = num(rate), y = num(years);
            if (i <= 0 || d < 0 || s < 0 || r < 0 || y < 1) { rentL.setText("Check inputs"); return; }
            double maxRent = Math.max(0, i * 0.30 - 0);
            double maxEmi = Math.max(0, i * 0.40 - d);
            double rm = r / 1200; int n = (int) y * 12;
            double loan = rm == 0 ? maxEmi * n : maxEmi * (Math.pow(1 + rm, n) - 1) / (rm * Math.pow(1 + rm, n));
            rentL.setText(Ui.money(maxRent) + " / month");
            emiL.setText("Max affordable EMI: " + Ui.money(maxEmi) + " / month");
            loanL.setText("Eligible loan: " + Ui.money(loan));
            priceL.setText("Property budget: " + Ui.money(loan + s));
        };
        Ui.RButton go = Ui.primary("Check"); go.addActionListener(e -> calc.run());
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        Ui.row(form, 0, "Monthly income (BDT)", inc); Ui.row(form, 1, "Existing monthly debts", debt);
        Ui.row(form, 2, "Savings for down payment", sav); Ui.row(form, 3, "Loan interest (% p.a.)", rate);
        Ui.row(form, 4, "Loan term (years)", years);
        GridBagConstraints g = new GridBagConstraints(); g.gridy = 5; g.gridx = 1; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(8, 4, 8, 4);
        form.add(go, g);
        JPanel res = Ui.transparent(new GridLayout(0, 1, 0, 8));
        res.add(Ui.muted("Recommended rent budget (30% of income)")); res.add(rentL); res.add(emiL); res.add(loanL); res.add(priceL);
        res.add(Ui.muted("Rule of thumb: EMI + debts should stay under 40% of income."));
        return twoCol(form, res, null, calc);
    }

    private JComponent twoCol(JPanel form, JPanel results, JPanel extra, Runnable calc) {
        JPanel p = new JPanel(new GridLayout(1, 2, 20, 0));
        p.setBackground(Ui.SURFACE); p.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel l = Ui.transparent(new BorderLayout()); l.add(form, BorderLayout.NORTH);
        JPanel r = Ui.transparent(new BorderLayout(0, 10)); r.add(results, BorderLayout.NORTH);
        if (extra != null) r.add(extra, BorderLayout.CENTER);
        p.add(l); p.add(r);
        calc.run();
        return p;
    }
}
