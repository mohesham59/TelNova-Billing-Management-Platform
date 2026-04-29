/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.telecom.billing.servlet;

import com.mycompany.telecom.billing.dao.*;
import com.mycompany.telecom.billing.model.*;
 
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
 
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 *
 * @author Ali
 */
@WebServlet("/portal/home")
public class PortalHomeServlet extends HttpServlet {
 
    private final ContractDAO    contractDAO  = new ContractDAO();
    private final RatePlanDAO    ratePlanDAO  = new RatePlanDAO();
    private final ConsumptionDAO consumDAO    = new ConsumptionDAO();
    private final BillDAO        billDAO      = new BillDAO();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        HttpSession session  = req.getSession(false);
        String userId        = (String) session.getAttribute("portalUserId");
        String userName      = (String) session.getAttribute("portalUserName");
        String userEmail     = (String) session.getAttribute("portalEmail");
        String ctx           = req.getContextPath();
 
        try {
            List<Contract>  contracts = contractDAO.findByUserId(userId);
            List<RatePlan>  allPlans  = ratePlanDAO.findAll();
 
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
 
            renderPageHeader(out, userName, userEmail, ctx);
            renderWelcomeBanner(out, userName, contracts.size());
 
            if (contracts.isEmpty()) {
                renderNoContracts(out);
            } else {
                // ── For each contract render a full section ───────────────────
                for (Contract ct : contracts) {
                    List<ConsumptionView> consumption = consumDAO.findByContractId(ct.getId());
                    List<BillSummary>     bills       = billDAO.findRecentByContractId(ct.getId());
                    RatePlan              plan        = ratePlanDAO.findById(ct.getRatePlanId());
                    renderContractSection(out, ct, plan, consumption, bills, ctx);
                }
            }
 
            // ── Available Rate Plans — shown once at the bottom ───────────────
            renderAllPlans(out, allPlans,
                    contracts.stream().mapToInt(Contract::getRatePlanId).toArray());
 
            renderPageFooter(out, ctx);
 
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // PAGE SHELL
    // ═══════════════════════════════════════════════════════════════════════════
 
    private void renderPageHeader(PrintWriter out, String userName, String email, String ctx) {
        out.print("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — TeleMeter</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            :root{
              --bg:#0d1117;--sf:#161b22;--s2:#21262d;--bd:#30363d;
              --green:#2ea44f;--blue:#58a6ff;--orange:#d29922;
              --red:#f85149;--purple:#a371f7;
              --text:#e6edf3;--muted:#8b949e;--r:12px;
            }
            body{font-family:'Inter',system-ui,sans-serif;background:var(--bg);color:var(--text);}
 
            /* Topbar */
            .topbar{background:var(--sf);border-bottom:1px solid var(--bd);height:58px;
                    padding:0 28px;display:flex;align-items:center;
                    justify-content:space-between;position:sticky;top:0;z-index:100;}
            .brand{display:flex;align-items:center;gap:10px;font-size:16px;font-weight:700;}
            .brand-icon{width:34px;height:34px;border-radius:9px;font-size:16px;
              background:linear-gradient(135deg,var(--green),var(--blue));
              display:flex;align-items:center;justify-content:center;}
            .topbar-right{display:flex;align-items:center;gap:12px;}
            .user-chip{background:var(--s2);border:1px solid var(--bd);
              padding:5px 14px;border-radius:20px;font-size:13px;font-weight:500;}
            .signout{color:var(--red);text-decoration:none;font-size:13px;font-weight:500;
              padding:6px 12px;border-radius:7px;transition:background .15s;}
            .signout:hover{background:rgba(248,81,73,.1);}
 
            /* Layout */
            .page{max-width:1080px;margin:0 auto;padding:28px 20px 60px;}
 
            /* Welcome banner */
            .welcome{background:linear-gradient(135deg,rgba(46,164,79,.15),rgba(88,166,255,.1));
              border:1px solid rgba(46,164,79,.25);border-radius:var(--r);
              padding:22px 26px;margin-bottom:28px;
              display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px;}
            .welcome-text h2{font-size:20px;font-weight:700;margin-bottom:4px;}
            .welcome-text p{font-size:13.5px;color:var(--muted);}
            .welcome-stats{display:flex;gap:20px;}
            .ws-item{text-align:center;}
            .ws-num{font-size:22px;font-weight:800;color:var(--blue);}
            .ws-lbl{font-size:11px;color:var(--muted);font-weight:500;text-transform:uppercase;letter-spacing:.5px;}
 
            /* Section divider */
            .sec-title{font-size:11.5px;font-weight:700;color:var(--muted);letter-spacing:1px;
              text-transform:uppercase;margin:32px 0 14px;
              display:flex;align-items:center;gap:10px;}
            .sec-title::after{content:'';flex:1;height:1px;background:var(--bd);}
 
            /* Contract card */
            .contract-card{background:var(--sf);border:1px solid var(--bd);
              border-radius:var(--r);margin-bottom:24px;overflow:hidden;}
            .contract-card-header{padding:16px 22px;background:var(--s2);
              border-bottom:1px solid var(--bd);
              display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px;}
            .contract-msisdn{font-size:17px;font-weight:700;letter-spacing:.5px;}
            .contract-meta{font-size:12.5px;color:var(--muted);margin-top:3px;}
            .contract-body{padding:20px;}
 
            /* Info grid */
            .info-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-bottom:22px;}
            .info-item{background:var(--s2);border:1px solid var(--bd);border-radius:9px;padding:14px;}
            .ii-label{font-size:10.5px;color:var(--muted);font-weight:600;
              letter-spacing:.5px;text-transform:uppercase;margin-bottom:6px;}
            .ii-value{font-size:17px;font-weight:700;}
            .ii-sub{font-size:11.5px;color:var(--muted);margin-top:3px;}
 
            /* Status badges */
            .badge{display:inline-flex;align-items:center;gap:5px;padding:4px 12px;
              border-radius:20px;font-size:12px;font-weight:600;}
            .b-active   {background:rgba(46,164,79,.15); color:#3fb950;border:1px solid rgba(46,164,79,.3);}
            .b-suspended{background:rgba(210,153,34,.15);color:#d29922;border:1px solid rgba(210,153,34,.3);}
            .b-deactive {background:rgba(248,81,73,.15); color:#f85149;border:1px solid rgba(248,81,73,.3);}
            .b-onhold   {background:rgba(139,148,158,.15);color:#8b949e;border:1px solid rgba(139,148,158,.3);}
 
            /* Credit bar */
            .credit-row{display:flex;align-items:center;gap:20px;flex-wrap:wrap;margin-bottom:22px;}
            .credit-numbers{min-width:160px;}
            .credit-big{font-size:30px;font-weight:800;color:var(--blue);}
            .credit-of{font-size:13px;color:var(--muted);margin-top:2px;}
            .credit-pct-label{font-size:13px;font-weight:600;margin-top:6px;}
            .credit-bar-wrap{flex:1;min-width:200px;}
            .prog-bg{height:10px;background:var(--s2);border-radius:99px;
              overflow:hidden;border:1px solid var(--bd);}
            .prog-fill{height:100%;border-radius:99px;transition:width .6s ease;width:0;}
            .fill-green {background:linear-gradient(90deg,#2ea44f,#3fb950);}
            .fill-blue  {background:linear-gradient(90deg,#58a6ff,#1f6feb);}
            .fill-orange{background:linear-gradient(90deg,#d29922,#bf8700);}
            .fill-red   {background:linear-gradient(90deg,#f85149,#da3633);}
            .fill-voice {background:linear-gradient(90deg,#a371f7,#7c5cbf);}
            .fill-sms   {background:linear-gradient(90deg,#d29922,#bf8700);}
            .prog-labels{display:flex;justify-content:space-between;
              font-size:11.5px;color:var(--muted);margin-top:5px;}
 
            /* Usage progress bars */
            .usage-section{margin-bottom:22px;}
            .usage-title{font-size:13px;font-weight:700;color:var(--muted);
              letter-spacing:.5px;text-transform:uppercase;margin-bottom:12px;}
            .usage-item{margin-bottom:18px;}
            .usage-item:last-child{margin-bottom:0;}
            .usage-top{display:flex;justify-content:space-between;align-items:center;margin-bottom:7px;}
            .usage-name{font-size:13.5px;font-weight:600;}
            .usage-nums{font-size:12px;color:var(--muted);}
            .usage-bot{display:flex;justify-content:space-between;
              font-size:11.5px;color:var(--muted);margin-top:5px;}
            .usage-empty{text-align:center;padding:28px;color:var(--muted);font-size:13.5px;}
 
            /* Bills table */
            .tbl-wrap{overflow-x:auto;margin-bottom:22px;}
            .tbl{width:100%;border-collapse:collapse;font-size:13.5px;}
            .tbl th{padding:10px 14px;text-align:left;font-size:11px;font-weight:600;
              color:var(--muted);letter-spacing:.7px;text-transform:uppercase;
              background:var(--s2);border-bottom:1px solid var(--bd);}
            .tbl td{padding:13px 14px;border-bottom:1px solid var(--bd);}
            .tbl tbody tr:hover{background:rgba(255,255,255,.02);}
            .tbl tbody tr:last-child td{border-bottom:none;}
            .amount{color:#3fb950;font-weight:700;}
            .chip{background:var(--s2);border:1px solid var(--bd);
              padding:3px 8px;border-radius:6px;font-size:12px;
              display:inline-block;margin:1px 2px;}
 
            /* Rate plan comparison */
            .plans-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:14px;}
            .plan-card{background:var(--sf);border:1px solid var(--bd);
              border-radius:var(--r);padding:20px;transition:border-color .2s;}
            .plan-card:hover{border-color:var(--blue);}
            .plan-card.active-plan{border-color:var(--green);background:rgba(46,164,79,.04);}
            .pc-tag{display:inline-block;background:rgba(46,164,79,.18);color:#3fb950;
              border:1px solid rgba(46,164,79,.35);font-size:10px;font-weight:700;
              padding:2px 8px;border-radius:20px;letter-spacing:.4px;margin-bottom:6px;}
            .pc-name{font-size:15px;font-weight:700;margin-bottom:4px;}
            .pc-fee{font-size:28px;font-weight:800;color:var(--blue);margin:10px 0 16px;}
            .pc-fee span{font-size:13px;font-weight:500;color:var(--muted);}
            .pc-row{display:flex;justify-content:space-between;font-size:12.5px;
              padding:6px 0;border-bottom:1px solid var(--bd);}
            .pc-row:last-of-type{border-bottom:none;}
            .pc-lbl{color:var(--muted);}
 
            /* No contracts */
            .no-contracts{text-align:center;padding:60px 20px;color:var(--muted);}
            .no-contracts .ni{font-size:48px;margin-bottom:14px;}
 
            @media(max-width:680px){
              .info-grid{grid-template-columns:1fr 1fr;}
              .credit-row{flex-direction:column;align-items:flex-start;}
              .welcome{flex-direction:column;}
            }
            </style></head><body>
            """);
 
        out.printf("""
            <div class='topbar'>
              <div class='brand'><div class='brand-icon'>📡</div>TeleMeter</div>
              <div class='topbar-right'>
                <span class='user-chip'>👤 %s</span>
                <a href='%s/portal/logout' class='signout'>Sign Out</a>
              </div>
            </div>
            <div class='page'>
            """, e(userName), ctx);
    }
 
    // ── Welcome banner ────────────────────────────────────────────────────────
    private void renderWelcomeBanner(PrintWriter out, String userName, int contractCount) {
        out.printf("""
            <div class='welcome'>
              <div class='welcome-text'>
                <h2>Welcome back, %s 👋</h2>
                <p>Here's a full overview of your account, usage, and billing.</p>
              </div>
              <div class='welcome-stats'>
                <div class='ws-item'>
                  <div class='ws-num'>%d</div>
                  <div class='ws-lbl'>Contract%s</div>
                </div>
              </div>
            </div>
            """, e(userName), contractCount, contractCount != 1 ? "s" : "");
    }
 
    // ── No contracts ──────────────────────────────────────────────────────────
    private void renderNoContracts(PrintWriter out) {
        out.print("""
            <div class='no-contracts'>
              <div class='ni'>📋</div>
              <p>You have no active contracts yet. Please contact support.</p>
            </div>
            """);
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // ONE CONTRACT SECTION
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderContractSection(PrintWriter out, Contract ct, RatePlan plan,
            List<ConsumptionView> consumption, List<BillSummary> bills, String ctx) {
 
        String statusBadge = switch (ct.getStatus()) {
            case "active"    -> "<span class='badge b-active'>● Active</span>";
            case "suspended" -> "<span class='badge b-suspended'>⏸ Suspended</span>";
            case "de-active" -> "<span class='badge b-deactive'>✕ De-active</span>";
            case "on-hold"   -> "<span class='badge b-onhold'>⏳ On Hold</span>";
            default          -> e(ct.getStatus());
        };
 
        // ── Contract card header ──────────────────────────────────────────────
        out.printf("""
            <div class='contract-card'>
              <div class='contract-card-header'>
                <div>
                  <div class='contract-msisdn'>📱 %s</div>
                  <div class='contract-meta'>Contract #%d · %s</div>
                </div>
                %s
              </div>
              <div class='contract-body'>
            """, e(ct.getMsisdn()), ct.getId(), e(ct.getPlanName()), statusBadge);
 
        // ── 1. Account info grid ──────────────────────────────────────────────
        out.print("<div class='info-grid'>");
        infoItem(out, "Rate Plan",      e(ct.getPlanName()), plan != null ? "$" + fmt(plan.getMonthlyFee()) + "/mo" : null);
        infoItem(out, "Activated On",   e(ct.getActivationDate()), null);
        infoItem(out, "Billing Day",    "Day " + ct.getBillingCycleDay(), "of each month");
        infoItem(out, "Available Credit", "<span style='color:var(--blue);font-size:20px;font-weight:800;'>$" + fmt(ct.getAvailableCredit()) + "</span>", "of $" + fmt(ct.getCreditLimit()) + " limit");
        out.print("</div>");
 
        // ── 2. Credit balance bar ─────────────────────────────────────────────
        renderCreditBar(out, ct);
 
        // ── 3. Current period usage ───────────────────────────────────────────
        renderUsageBars(out, consumption);
 
        // ── 4. Recent bills ───────────────────────────────────────────────────
        renderBills(out, bills);
 
        out.print("</div></div>"); // close contract-body + contract-card
    }
 
    // ── Credit balance bar ────────────────────────────────────────────────────
    private void renderCreditBar(PrintWriter out, Contract ct) {
        BigDecimal avail = ct.getAvailableCredit() != null ? ct.getAvailableCredit() : BigDecimal.ZERO;
        BigDecimal limit = ct.getCreditLimit()     != null ? ct.getCreditLimit()     : BigDecimal.ZERO;
 
        int pct = 0;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            pct = avail.multiply(BigDecimal.valueOf(100))
                       .divide(limit, 0, RoundingMode.HALF_UP)
                       .intValue();
            pct = Math.min(pct, 100);
        }
 
        String fillCls  = pct < 20 ? "fill-red" : pct < 50 ? "fill-orange" : "fill-green";
        String pctColor = pct < 20 ? "var(--red)" : pct < 50 ? "var(--orange)" : "var(--green)";
 
        out.printf("""
            <div class='credit-row'>
              <div class='credit-numbers'>
                <div class='credit-big'>$%s</div>
                <div class='credit-of'>available of $%s limit</div>
                <div class='credit-pct-label' style='color:%s;'>%d%% remaining</div>
              </div>
              <div class='credit-bar-wrap'>
                <div class='prog-bg'>
                  <div class='prog-fill %s' data-width='%d' style='height:10px;'></div>
                </div>
                <div class='prog-labels'><span>$0</span><span>$%s</span></div>
              </div>
            </div>
            """, fmt(avail), fmt(limit), pctColor, pct, fillCls, pct, fmt(limit));
    }
 
    // ── Usage progress bars ───────────────────────────────────────────────────
    private void renderUsageBars(PrintWriter out, List<ConsumptionView> consumption) {
        out.print("<div class='usage-section'>");
        out.print("<div class='usage-title'>📊 Current Period Usage</div>");
 
        if (consumption.isEmpty()) {
            out.print("<div class='usage-empty'>No usage recorded for this billing period yet.</div>");
        } else {
            for (ConsumptionView cv : consumption) {
                int pct = cv.getPercentage();
                String icon = switch (cv.getServiceType()) {
                    case "voice" -> "🎙";
                    case "data"  -> "📶";
                    case "sms"   -> "💬";
                    default      -> "📦";
                };
                String fillCls = pct >= 90 ? "fill-red"
                        : switch (cv.getServiceType()) {
                            case "voice" -> "fill-voice";
                            case "data"  -> "fill-blue";
                            case "sms"   -> "fill-sms";
                            default      -> "fill-blue";
                        };
 
                out.printf("""
                    <div class='usage-item'>
                      <div class='usage-top'>
                        <span class='usage-name'>%s %s</span>
                        <span class='usage-nums'>%s / %s %s &nbsp;·&nbsp; <strong>%d%%</strong> used</span>
                      </div>
                      <div class='prog-bg'>
                        <div class='prog-fill %s' data-width='%d'></div>
                      </div>
                      <div class='usage-bot'>
                        <span>%s %s remaining</span>
                        <span>Since %s</span>
                      </div>
                    </div>
                    """,
                        icon, e(cv.getPackageName()),
                        fmt(cv.getConsumed()), fmt(cv.getTotalQuota()), cv.getUnit(),
                        pct, fillCls, pct,
                        fmt(cv.getRemaining()), cv.getUnit(),
                        cv.getStartingDate() != null ? cv.getStartingDate() : "—");
            }
        }
        out.print("</div>"); // usage-section
    }
 
    // ── Bills table ───────────────────────────────────────────────────────────
    private void renderBills(PrintWriter out, List<BillSummary> bills) {
        out.print("<div class='usage-title'>🧾 Recent Bills</div>");
 
        if (bills.isEmpty()) {
            out.print("<div class='usage-empty'>No bills generated yet.</div>");
            return;
        }
 
        out.print("""
            <div class='tbl-wrap'>
            <table class='tbl'>
              <thead><tr>
                <th>Period</th>
                <th>Usage</th>
                <th>Recurring</th>
                <th>One-Time</th>
                <th>Tax</th>
                <th>Total</th>
              </tr></thead><tbody>
            """);
 
        for (BillSummary b : bills) {
            String period = (b.getPeriodStart() != null && b.getPeriodEnd() != null)
                    ? b.getPeriodStart() + " → " + b.getPeriodEnd()
                    : (b.getBillingDate() != null ? b.getBillingDate().toString() : "—");
 
            out.printf("""
                <tr>
                  <td>
                    <div style='font-weight:600;'>%s</div>
                    %s
                  </td>
                  <td>
                    <span class='chip'>🎙 %s</span>
                    <span class='chip'>📶 %d MB</span>
                    <span class='chip'>💬 %d</span>
                  </td>
                  <td>$%s</td>
                  <td>$%s</td>
                  <td style='color:var(--muted);'>$%s</td>
                  <td class='amount'>$%s</td>
                </tr>
                """,
                    period,
                    b.getBillingDate() != null
                            ? "<div style='font-size:11.5px;color:var(--muted);'>Billed " + b.getBillingDate() + "</div>"
                            : "",
                    b.getVoiceFormatted(),
                    b.getDataUsage(),
                    b.getSmsUsage(),
                    fmt(b.getRecurringFees()),
                    fmt(b.getOneTimeFees()),
                    fmt(b.getTaxes()),
                    fmt(b.getTotalAmount()));
        }
 
        out.print("</tbody></table></div>");
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // AVAILABLE RATE PLANS (shown once at the bottom)
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderAllPlans(PrintWriter out, List<RatePlan> plans, int[] activePlanIds) {
        out.print("<div class='sec-title'>💳 Available Rate Plans</div>");
        out.print("<div class='plans-grid'>");
 
        for (RatePlan rp : plans) {
            boolean isActive = false;
            for (int id : activePlanIds) {
                if (id == rp.getId()) { isActive = true; break; }
            }
 
            out.printf("""
                <div class='plan-card %s'>
                  %s
                  <div class='pc-name'>%s</div>
                  <div class='pc-fee'>$%s<span>/month</span></div>
                  <div class='pc-row'><span class='pc-lbl'>📶 Data ROR</span><span>%s%%</span></div>
                  <div class='pc-row'><span class='pc-lbl'>🎙 Voice ROR</span><span>%s%%</span></div>
                  <div class='pc-row'><span class='pc-lbl'>💬 SMS ROR</span><span>%s%%</span></div>
                </div>
                """,
                    isActive ? "active-plan" : "",
                    isActive ? "<div class='pc-tag'>✓ YOUR PLAN</div>" : "",
                    e(rp.getPlanName()),
                    fmt(rp.getMonthlyFee()),
                    fmt(rp.getRorData()),
                    fmt(rp.getRorVoice()),
                    fmt(rp.getRorSms()));
        }
 
        out.print("</div>");
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // PAGE FOOTER
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderPageFooter(PrintWriter out, String ctx) {
        out.print("""
            </div>
            <script>
              document.querySelectorAll('.prog-fill').forEach(bar => {
                const w = bar.getAttribute('data-width') || '0';
                bar.style.width = '0';
                requestAnimationFrame(() => setTimeout(() => bar.style.width = w + '%', 80));
              });
            </script>
            </body></html>
            """);
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
    private void infoItem(PrintWriter out, String label, String value, String sub) {
        out.printf("""
            <div class='info-item'>
              <div class='ii-label'>%s</div>
              <div class='ii-value'>%s</div>
              %s
            </div>
            """, label, value,
                sub != null ? "<div class='ii-sub'>" + sub + "</div>" : "");
    }
 
    private String e(Object val) {
        if (val == null) return "—";
        return val.toString()
                .replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
 
    private String fmt(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
