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

@WebServlet("/portal/home")
public class PortalHomeServlet extends HttpServlet {
 
    private static final String EGP = "EGP";
 
    private final ContractDAO    contractDAO = new ContractDAO();
    private final RatePlanDAO    ratePlanDAO = new RatePlanDAO();
    private final ConsumptionDAO consumDAO   = new ConsumptionDAO();
    private final BillDAO        billDAO     = new BillDAO();
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        HttpSession session = req.getSession(false);
        String userId   = (String) session.getAttribute("portalUserId");
        String userName = (String) session.getAttribute("portalUserName");
        String ctx      = req.getContextPath();
 
        // Which tab is active?
        String tab = req.getParameter("tab");
        if (tab == null) tab = "overview";
 
        try {
            List<Contract> contracts = contractDAO.findByUserId(userId);
            List<RatePlan> allPlans  = ratePlanDAO.findAll();
            List<BillSummary> allBills = billDAO.findAllByUserId(userId);
 
            resp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = resp.getWriter();
 
            renderHeader(out, userName, ctx, tab);
            renderWelcomeBanner(out, userName, contracts.size(), allBills.size());
 
            if ("invoices".equals(tab)) {
                renderInvoicesTab(out, allBills, ctx);
            } else if ("plans".equals(tab)) {
                renderPlansTab(out, allPlans,
                        contracts.stream().mapToInt(Contract::getRatePlanId).toArray());
            } else {
                // Default: overview — show all contracts
                if (contracts.isEmpty()) {
                    renderNoContracts(out);
                } else {
                    for (Contract ct : contracts) {
                        List<ConsumptionView> consumption = consumDAO.findByContractId(ct.getId());
                        List<BillSummary>     bills       = billDAO.findRecentByContractId(ct.getId());
                        RatePlan              plan        = ratePlanDAO.findById(ct.getRatePlanId());
                        renderContractSection(out, ct, plan, consumption, bills, ctx);
                    }
                }
            }
 
            renderFooter(out);
 
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // PAGE HEADER + CSS + NAV TABS
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderHeader(PrintWriter out, String userName, String ctx, String activeTab) {
        out.print("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — Telnova</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            :root{
              --bg:#0a0e27;--sf:#1a1f3a;--s2:#1f2547;--bd:#2d3561;
              --cyan:#00d4ff;--indigo:#6366f1;--green:#10b981;
              --orange:#f59e0b;--red:#ef4444;--purple:#a78bfa;
              --text:#e8eaf6;--muted:#9ca3af;--r:12px;
            }
            body{font-family:'Inter',system-ui,sans-serif;background:var(--bg);color:var(--text);}
 
            /* Topbar */
            .topbar{background:var(--sf);border-bottom:1px solid var(--bd);height:58px;
              padding:0 28px;display:flex;align-items:center;
              justify-content:space-between;position:sticky;top:0;z-index:100;}
            .brand{display:flex;align-items:center;gap:10px;font-size:17px;font-weight:700;}
            .brand svg{flex-shrink:0;}
            .brand .tel{color:#fff;}
            .brand .nova{background:linear-gradient(135deg,var(--cyan),var(--indigo));
              -webkit-background-clip:text;-webkit-text-fill-color:transparent;}
            .topbar-right{display:flex;align-items:center;gap:12px;}
            .user-chip{background:var(--s2);border:1px solid var(--bd);
              padding:5px 14px;border-radius:20px;font-size:13px;font-weight:500;}
            .signout{color:var(--red);text-decoration:none;font-size:13px;font-weight:500;
              padding:6px 12px;border-radius:7px;transition:background .15s;}
            .signout:hover{background:rgba(239,68,68,.1);}
 
            /* Tab nav */
            .tab-bar{background:var(--sf);border-bottom:1px solid var(--bd);
              padding:0 28px;display:flex;gap:4px;}
            .tab-link{padding:14px 18px;font-size:13.5px;font-weight:500;
              color:var(--muted);text-decoration:none;border-bottom:2px solid transparent;
              transition:all .15s;display:flex;align-items:center;gap:7px;}
            .tab-link:hover{color:var(--text);}
            .tab-link.active{color:var(--cyan);border-bottom-color:var(--cyan);font-weight:600;}
 
            /* Layout */
            .page{max-width:1080px;margin:0 auto;padding:28px 20px 60px;}
 
            /* Welcome banner */
            .welcome{background:linear-gradient(135deg,rgba(99,102,241,.15),rgba(0,212,255,.08));
              border:1px solid rgba(99,102,241,.25);border-radius:var(--r);
              padding:22px 26px;margin-bottom:28px;
              display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px;}
            .welcome h2{font-size:20px;font-weight:700;margin-bottom:4px;}
            .welcome p{font-size:13.5px;color:var(--muted);}
            .welcome-stats{display:flex;gap:24px;}
            .ws-num{font-size:22px;font-weight:800;color:var(--cyan);}
            .ws-lbl{font-size:11px;color:var(--muted);font-weight:500;text-transform:uppercase;letter-spacing:.5px;}
 
            /* Section label */
            .sec{font-size:11px;font-weight:700;color:var(--muted);letter-spacing:1px;
              text-transform:uppercase;margin:32px 0 14px;
              display:flex;align-items:center;gap:10px;}
            .sec::after{content:'';flex:1;height:1px;background:var(--bd);}
 
            /* Contract card */
            .ct-card{background:var(--sf);border:1px solid var(--bd);border-radius:var(--r);margin-bottom:24px;}
            .ct-head{padding:16px 22px;background:var(--s2);border-bottom:1px solid var(--bd);
              display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px;
              border-radius:var(--r) var(--r) 0 0;}
            .ct-msisdn{font-size:17px;font-weight:700;}
            .ct-meta{font-size:12.5px;color:var(--muted);margin-top:3px;}
            .ct-body{padding:20px;}
 
            /* Info grid */
            .info-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(155px,1fr));gap:12px;margin-bottom:22px;}
            .ii{background:var(--s2);border:1px solid var(--bd);border-radius:9px;padding:14px;}
            .ii-lbl{font-size:10px;color:var(--muted);font-weight:700;
              letter-spacing:.6px;text-transform:uppercase;margin-bottom:6px;}
            .ii-val{font-size:17px;font-weight:700;}
            .ii-sub{font-size:11.5px;color:var(--muted);margin-top:3px;}
 
            /* Badges */
            .badge{display:inline-flex;align-items:center;gap:5px;padding:4px 12px;
              border-radius:20px;font-size:12px;font-weight:600;}
            .b-active   {background:rgba(16,185,129,.12);color:#10b981;border:1px solid rgba(16,185,129,.3);}
            .b-suspended{background:rgba(245,158,11,.12);color:#f59e0b;border:1px solid rgba(245,158,11,.3);}
            .b-deactive {background:rgba(239,68,68,.12); color:#ef4444;border:1px solid rgba(239,68,68,.3);}
            .b-onhold   {background:rgba(156,163,175,.12);color:#9ca3af;border:1px solid rgba(156,163,175,.3);}
 
            /* Progress bars */
            .pg-wrap{margin-bottom:18px;}
            .pg-wrap:last-child{margin-bottom:0;}
            .pg-top{display:flex;justify-content:space-between;align-items:center;margin-bottom:7px;}
            .pg-name{font-size:13.5px;font-weight:600;}
            .pg-nums{font-size:12px;color:var(--muted);}
            .pg-bg{height:9px;background:var(--s2);border-radius:99px;overflow:hidden;border:1px solid var(--bd);}
            .pg-fill{height:100%;border-radius:99px;transition:width .6s ease;width:0;}
            .fill-cyan   {background:linear-gradient(90deg,#00d4ff,#0ea5e9);}
            .fill-indigo {background:linear-gradient(90deg,#6366f1,#8b5cf6);}
            .fill-orange {background:linear-gradient(90deg,#f59e0b,#d97706);}
            .fill-red    {background:linear-gradient(90deg,#ef4444,#dc2626);}
            .pg-bot{display:flex;justify-content:space-between;font-size:11.5px;color:var(--muted);margin-top:5px;}
 
            /* Credit */
            .credit-row{display:flex;align-items:center;gap:20px;flex-wrap:wrap;margin-bottom:22px;}
            .credit-big{font-size:30px;font-weight:800;color:var(--cyan);}
            .credit-of{font-size:13px;color:var(--muted);margin-top:2px;}
            .credit-pct{font-size:13px;font-weight:600;margin-top:6px;}
            .credit-bar{flex:1;min-width:200px;}
            .bar-labels{display:flex;justify-content:space-between;font-size:11.5px;color:var(--muted);margin-top:5px;}
 
            /* Bills table */
            .tbl-wrap{overflow-x:auto;margin-bottom:22px;}
            .tbl{width:100%;border-collapse:collapse;font-size:13px;}
            .tbl th{padding:10px 14px;text-align:left;font-size:10.5px;font-weight:700;
              color:var(--indigo);letter-spacing:.7px;text-transform:uppercase;
              background:var(--s2);border-bottom:1px solid var(--bd);}
            .tbl td{padding:12px 14px;border-bottom:1px solid var(--bd);}
            .tbl tbody tr:hover{background:rgba(99,102,241,.04);}
            .tbl tbody tr:last-child td{border-bottom:none;}
            .amount{color:var(--green);font-weight:700;}
            .chip{background:var(--s2);border:1px solid var(--bd);
              padding:3px 8px;border-radius:6px;font-size:11.5px;display:inline-block;margin:1px 2px;}
 
            /* Invoice download button */
            .dl-btn{display:inline-flex;align-items:center;gap:5px;
              padding:5px 12px;border-radius:6px;font-size:12px;font-weight:600;
              background:linear-gradient(135deg,var(--indigo),var(--cyan));
              color:#fff;text-decoration:none;transition:opacity .15s;}
            .dl-btn:hover{opacity:.85;}
 
            /* Invoices tab */
            .inv-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:16px;}
            .inv-card{background:var(--sf);border:1px solid var(--bd);border-radius:var(--r);padding:20px;
              display:flex;flex-direction:column;gap:12px;}
            .inv-head{display:flex;align-items:center;justify-content:space-between;}
            .inv-id{font-size:15px;font-weight:700;}
            .inv-period{font-size:12px;color:var(--muted);}
            .inv-amount{font-size:24px;font-weight:800;color:var(--cyan);}
            .inv-detail{font-size:12px;color:var(--muted);display:flex;gap:12px;flex-wrap:wrap;}
 
            /* Plans grid */
            .plans-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:14px;}
            .plan-card{background:var(--sf);border:1px solid var(--bd);border-radius:var(--r);
              padding:20px;transition:border-color .2s;}
            .plan-card:hover{border-color:var(--cyan);}
            .plan-card.mine{border-color:var(--green);background:rgba(16,185,129,.04);}
            .mine-tag{display:inline-block;background:rgba(16,185,129,.15);color:#10b981;
              border:1px solid rgba(16,185,129,.35);font-size:10px;font-weight:700;
              padding:2px 9px;border-radius:20px;margin-bottom:6px;}
            .pc-name{font-size:15px;font-weight:700;margin-bottom:4px;}
            .pc-fee{font-size:28px;font-weight:800;color:var(--cyan);margin:10px 0 16px;}
            .pc-fee span{font-size:13px;font-weight:500;color:var(--muted);}
            .pc-row{display:flex;justify-content:space-between;font-size:12.5px;
              padding:6px 0;border-bottom:1px solid var(--bd);}
            .pc-row:last-of-type{border-bottom:none;}
            .pc-lbl{color:var(--muted);}
 
            /* Empty */
            .empty-state{text-align:center;padding:60px 20px;color:var(--muted);}
            .empty-state .ei{font-size:48px;margin-bottom:14px;}
 
            @media(max-width:680px){
              .info-grid{grid-template-columns:1fr 1fr;}
              .credit-row{flex-direction:column;align-items:flex-start;}
              .welcome{flex-direction:column;}
              .tab-link{padding:12px 12px;font-size:12.5px;}
            }
            </style></head><body>
            """);
 
        // Topbar
        out.printf("""
            <div class='topbar'>
              <div class='brand'>
                <svg viewBox='0 0 100 100' style='width:28px;height:28px;'>
                  <defs><linearGradient id='g1' x1='0%%' y1='0%%' x2='100%%' y2='100%%'>
                    <stop offset='0%%' stop-color='#00d4ff'/>
                    <stop offset='100%%' stop-color='#6366f1'/>
                  </linearGradient></defs>
                  <path d='M 15 60 L 35 45 L 50 50 L 65 30 L 80 25 L 70 45 L 85 50 L 55 60 L 50 75 L 35 60 Z'
                        fill='url(#g1)'/>
                </svg>
                <span><span class='tel'>Tel</span><span class='nova'>nova</span></span>
              </div>
              <div class='topbar-right'>
                <span class='user-chip'>👤 %s</span>
                <a href='%s/portal/logout' class='signout'>Sign Out</a>
              </div>
            </div>
            """, e(userName), ctx);
 
        // Tab bar
        String baseUrl = ctx + "/portal/home";
        out.printf("""
            <div class='tab-bar'>
              <a href='%s' class='tab-link %s'>📋 My Contracts</a>
              <a href='%s?tab=invoices' class='tab-link %s'>🧾 Invoices</a>
              <a href='%s?tab=plans' class='tab-link %s'>💳 Available Plans</a>
            </div>
            <div class='page'>
            """,
                baseUrl,       "overview".equals(activeTab) ? "active" : "",
                baseUrl,       "invoices".equals(activeTab) ? "active" : "",
                baseUrl,       "plans".equals(activeTab)    ? "active" : "");
    }
 
    // ── Welcome banner ────────────────────────────────────────────────────────
    private void renderWelcomeBanner(PrintWriter out, String userName,
                                     int contractCount, int billCount) {
        out.printf("""
            <div class='welcome'>
              <div>
                <h2>Welcome back, %s 👋</h2>
                <p>Manage your Telnova account, usage, and invoices below.</p>
              </div>
              <div class='welcome-stats'>
                <div style='text-align:center;'>
                  <div class='ws-num'>%d</div>
                  <div class='ws-lbl'>Contract%s</div>
                </div>
                <div style='text-align:center;'>
                  <div class='ws-num'>%d</div>
                  <div class='ws-lbl'>Invoice%s</div>
                </div>
              </div>
            </div>
            """, e(userName),
                contractCount, contractCount != 1 ? "s" : "",
                billCount,     billCount     != 1 ? "s" : "");
    }
 
    // ── No contracts ──────────────────────────────────────────────────────────
    private void renderNoContracts(PrintWriter out) {
        out.print("""
            <div class='empty-state'>
              <div class='ei'>📋</div>
              <p>You have no active contracts. Please contact Telnova support.</p>
            </div>
            """);
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 1 — OVERVIEW: one section per contract
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderContractSection(PrintWriter out, Contract ct, RatePlan plan,
            List<ConsumptionView> consumption, List<BillSummary> bills, String ctx) {
 
        String badge = switch (ct.getStatus()) {
            case "active"    -> "<span class='badge b-active'>● Active</span>";
            case "suspended" -> "<span class='badge b-suspended'>⏸ Suspended</span>";
            case "de-active" -> "<span class='badge b-deactive'>✕ De-active</span>";
            case "on-hold"   -> "<span class='badge b-onhold'>⏳ On Hold</span>";
            default          -> e(ct.getStatus());
        };
 
        out.printf("""
            <div class='ct-card'>
              <div class='ct-head'>
                <div>
                  <div class='ct-msisdn'>📱 %s</div>
                  <div class='ct-meta'>Contract #%d · %s</div>
                </div>
                %s
              </div>
              <div class='ct-body'>
            """, e(ct.getMsisdn()), ct.getId(), e(ct.getPlanName()), badge);
 
        // ── Info grid ─────────────────────────────────────────────────────────
        out.print("<div class='info-grid'>");
        ii(out, "Rate Plan",       e(ct.getPlanName()),
                plan != null ? EGP + " " + fmt(plan.getMonthlyFee()) + "/mo" : null);
        ii(out, "Activated On",    e(ct.getActivationDate()), null);
        ii(out, "Billing Day",     "Day " + ct.getBillingCycleDay(), "of each month");
        ii(out, "Available Credit",
                "<span style='color:var(--cyan);font-weight:800;'>" + EGP + " " + fmt(ct.getAvailableCredit()) + "</span>",
                "of " + EGP + " " + fmt(ct.getCreditLimit()) + " limit");
        out.print("</div>");
 
        // ── Credit gauge ──────────────────────────────────────────────────────
        BigDecimal avail = ct.getAvailableCredit() != null ? ct.getAvailableCredit() : BigDecimal.ZERO;
        BigDecimal limit = ct.getCreditLimit()     != null ? ct.getCreditLimit()     : BigDecimal.ZERO;
        int cpct = 0;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            cpct = Math.min(avail.multiply(BigDecimal.valueOf(100))
                    .divide(limit, 0, RoundingMode.HALF_UP).intValue(), 100);
        }
        String cfill  = cpct < 20 ? "fill-red" : cpct < 50 ? "fill-orange" : "fill-cyan";
        String ccolor = cpct < 20 ? "var(--red)" : cpct < 50 ? "var(--orange)" : "var(--cyan)";
 
        out.printf("""
            <div class='credit-row'>
              <div>
                <div class='credit-big'>%s %s</div>
                <div class='credit-of'>available of %s %s limit</div>
                <div class='credit-pct' style='color:%s;'>%d%% remaining</div>
              </div>
              <div class='credit-bar'>
                <div class='pg-bg' style='height:10px;'>
                  <div class='pg-fill %s' data-width='%d' style='height:10px;'></div>
                </div>
                <div class='bar-labels'><span>%s 0</span><span>%s %s</span></div>
              </div>
            </div>
            """, EGP, fmt(avail), EGP, fmt(limit), ccolor, cpct,
                cfill, cpct, EGP, EGP, fmt(limit));
 
        // ── Usage bars ────────────────────────────────────────────────────────
        out.print("<div style='margin-bottom:8px;font-size:11px;font-weight:700;color:var(--muted);letter-spacing:1px;text-transform:uppercase;'>📊 Current Period Usage</div>");
        if (consumption.isEmpty()) {
            out.print("<div style='text-align:center;padding:24px;color:var(--muted);font-size:13.5px;'>No usage recorded this period yet.</div>");
        } else {
            for (ConsumptionView cv : consumption) {
                int pct = cv.getPercentage();
                String icon = switch (cv.getServiceType()) {
                    case "voice" -> "🎙"; case "data" -> "📶"; case "sms" -> "💬"; default -> "📦";
                };
                String fill = pct >= 90 ? "fill-red"
                        : switch (cv.getServiceType()) {
                            case "voice" -> "fill-indigo";
                            case "data"  -> "fill-cyan";
                            case "sms"   -> "fill-orange";
                            default      -> "fill-cyan";
                        };
                out.printf("""
                    <div class='pg-wrap'>
                      <div class='pg-top'>
                        <span class='pg-name'>%s %s</span>
                        <span class='pg-nums'>%s / %s %s &nbsp;·&nbsp; <strong>%d%%</strong> used</span>
                      </div>
                      <div class='pg-bg'><div class='pg-fill %s' data-width='%d'></div></div>
                      <div class='pg-bot'>
                        <span>%s %s remaining</span>
                        <span>Since %s</span>
                      </div>
                    </div>
                    """,
                        icon, e(cv.getPackageName()),
                        fmt(cv.getConsumed()), fmt(cv.getTotalQuota()), cv.getUnit(),
                        pct, fill, pct,
                        fmt(cv.getRemaining()), cv.getUnit(),
                        cv.getStartingDate() != null ? cv.getStartingDate() : "—");
            }
        }
 
        // ── Recent bills for this contract ────────────────────────────────────
        out.print("<div style='margin:20px 0 10px;font-size:11px;font-weight:700;color:var(--muted);letter-spacing:1px;text-transform:uppercase;'>🧾 Recent Bills</div>");
        if (bills.isEmpty()) {
            out.print("<div style='text-align:center;padding:20px;color:var(--muted);font-size:13.5px;'>No bills yet.</div>");
        } else {
            out.print("<div class='tbl-wrap'><table class='tbl'><thead><tr><th>Period</th><th>Usage</th><th>Recurring</th><th>One-Time</th><th>Tax</th><th>Total</th><th>Invoice</th></tr></thead><tbody>");
            for (BillSummary b : bills) {
                String period = (b.getPeriodStart() != null && b.getPeriodEnd() != null)
                        ? b.getPeriodStart() + " → " + b.getPeriodEnd()
                        : (b.getBillingDate() != null ? b.getBillingDate().toString() : "—");
                out.printf("""
                    <tr>
                      <td><div style='font-weight:600;'>%s</div>
                          %s</td>
                      <td>
                        <span class='chip'>🎙 %s</span>
                        <span class='chip'>📶 %d MB</span>
                        <span class='chip'>💬 %d</span>
                      </td>
                      <td>%s %s</td>
                      <td>%s %s</td>
                      <td style='color:var(--muted);'>%s %s</td>
                      <td class='amount'>%s %s</td>
                      <td><a href='%s/portal/invoice/%d' class='dl-btn'>⬇ PDF</a></td>
                    </tr>
                    """,
                        period,
                        b.getBillingDate() != null
                                ? "<div style='font-size:11px;color:var(--muted);'>Billed " + b.getBillingDate() + "</div>"
                                : "",
                        b.getVoiceFormatted(), b.getDataUsage(), b.getSmsUsage(),
                        EGP, fmt(b.getRecurringFees()),
                        EGP, fmt(b.getOneTimeFees()),
                        EGP, fmt(b.getTaxes()),
                        EGP, fmt(b.getTotalAmount()),
                        ctx, b.getId());
            }
            out.print("</tbody></table></div>");
        }
 
        out.print("</div></div>"); // close ct-body + ct-card
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 2 — ALL INVOICES
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderInvoicesTab(PrintWriter out, List<BillSummary> bills, String ctx) {
        out.print("<div class='sec'>🧾 All Invoices</div>");
 
        if (bills.isEmpty()) {
            out.print("""
                <div class='empty-state'>
                  <div class='ei'>🧾</div>
                  <p>No invoices generated yet.</p>
                </div>
                """);
            return;
        }
 
        out.print("<div class='inv-grid'>");
        for (BillSummary b : bills) {
            String period = (b.getPeriodStart() != null && b.getPeriodEnd() != null)
                    ? b.getPeriodStart() + " → " + b.getPeriodEnd()
                    : (b.getBillingDate() != null ? b.getBillingDate().toString() : "—");
 
            out.printf("""
                <div class='inv-card'>
                  <div class='inv-head'>
                    <div>
                      <div class='inv-id'>Invoice #%d</div>
                      <div class='inv-period'>%s</div>
                    </div>
                    <a href='%s/portal/invoice/%d' class='dl-btn'>⬇ Download PDF</a>
                  </div>
                  <div class='inv-amount'>%s %s</div>
                  <div class='inv-detail'>
                    <span>🎙 %s</span>
                    <span>📶 %d MB</span>
                    <span>💬 %d msg</span>
                    <span>Tax: %s %s</span>
                  </div>
                  <div style='font-size:11.5px;color:var(--muted);'>
                    Billed: %s
                  </div>
                </div>
                """,
                    b.getId(), period,
                    ctx, b.getId(),
                    EGP, fmt(b.getTotalAmount()),
                    b.getVoiceFormatted(), b.getDataUsage(), b.getSmsUsage(),
                    EGP, fmt(b.getTaxes()),
                    b.getBillingDate() != null ? b.getBillingDate() : "—");
        }
        out.print("</div>");
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // TAB 3 — AVAILABLE PLANS
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderPlansTab(PrintWriter out, List<RatePlan> plans, int[] activePlanIds) {
        out.print("<div class='sec'>💳 Available Rate Plans</div>");
        out.print("<div class='plans-grid'>");
 
        for (RatePlan rp : plans) {
            boolean mine = false;
            for (int id : activePlanIds) if (id == rp.getId()) { mine = true; break; }
 
            out.printf("""
                <div class='plan-card %s'>
                  %s
                  <div class='pc-name'>%s</div>
                  <div class='pc-fee'>%s %s<span>/month</span></div>
                  <div class='pc-row'><span class='pc-lbl'>📶 Data ROR</span><span>%s pt</span></div>
                  <div class='pc-row'><span class='pc-lbl'>🎙 Voice ROR</span><span>%s pt</span></div>
                  <div class='pc-row'><span class='pc-lbl'>💬 SMS ROR</span><span>%s pt</span></div>
                </div>
                """,
                    mine ? "mine" : "",
                    mine ? "<div class='mine-tag'>✓ YOUR PLAN</div>" : "",
                    e(rp.getPlanName()),
                    EGP, fmt(rp.getMonthlyFee()),
                    fmt(rp.getRorData()), fmt(rp.getRorVoice()), fmt(rp.getRorSms()));
        }
        out.print("</div>");
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // FOOTER
    // ═══════════════════════════════════════════════════════════════════════════
    private void renderFooter(PrintWriter out) {
        out.print("""
            </div>
            <script>
              document.querySelectorAll('.pg-fill').forEach(bar => {
                const w = bar.getAttribute('data-width') || '0';
                bar.style.width = '0';
                requestAnimationFrame(() => setTimeout(() => bar.style.width = w + '%', 80));
              });
            </script>
            </body></html>
            """);
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
    private void ii(PrintWriter out, String label, String value, String sub) {
        out.printf("""
            <div class='ii'>
              <div class='ii-lbl'>%s</div>
              <div class='ii-val'>%s</div>
              %s
            </div>
            """, label, value, sub != null ? "<div class='ii-sub'>" + sub + "</div>" : "");
    }
 
    private String e(Object val) {
        if (val == null) return "—";
        return val.toString()
                .replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;");
    }
 
    private String fmt(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
 
