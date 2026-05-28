package com.mycompany.telecom.billing.util;

/**
 * Customer-facing portal layout utility.
 * Telnova brand — dark navy theme, cyan/indigo gradient, EGP currency.
 *
 * @author Ali
 */
public class CustomerLayout {

    // ── Brand constants ────────────────────────────────────────────────────────

    /** Egyptian Pound symbol used across every money value in the portal. */
    public static final String EGP = "EGP";

    /**
     * Telnova falcon SVG icon — dark background variant (cyan → indigo gradient).
     * Use this inside dark topbars and card headers.
     */
    public static final String LOGO_SVG =
        "<svg viewBox='0 0 100 100' style='width:32px;height:32px;flex-shrink:0;'>" +
        "<defs>" +
        "<linearGradient id='cl-falcon' x1='0%' y1='0%' x2='100%' y2='100%'>" +
        "<stop offset='0%' stop-color='#00d4ff'/>" +
        "<stop offset='100%' stop-color='#a78bfa'/>" +
        "</linearGradient>" +
        "</defs>" +
        "<path d='M 15 60 L 35 45 L 50 50 L 65 30 L 80 25 L 70 45 L 85 50 L 55 60 L 50 75 L 35 60 Z'" +
        "      fill='url(#cl-falcon)'/>" +
        "<path d='M 35 45 L 50 50 L 35 60 Z' fill='#0a0e27' opacity='0.4'/>" +
        "</svg>";

    /**
     * Telnova wordmark HTML — "Tel" white, "nova" cyan→indigo gradient text.
     */
    public static final String LOGO_WORDMARK =
        "<span style='color:#ffffff;font-weight:700;font-size:18px;letter-spacing:-.3px;'>Tel</span>" +
        "<span style='background:linear-gradient(135deg,#00d4ff,#a78bfa);" +
        "-webkit-background-clip:text;-webkit-text-fill-color:transparent;" +
        "font-weight:700;font-size:18px;letter-spacing:-.3px;'>nova</span>";

    // ── CSS ────────────────────────────────────────────────────────────────────
    private static final String CSS = """
        :root {
            /* Telnova brand palette */
            --bg:       #0a0e27;
            --surface:  #1a1f3a;
            --surface2: #1f2547;
            --border:   #2d3561;
            --cyan:     #00d4ff;
            --indigo:   #6366f1;
            --purple:   #a78bfa;
            --green:    #10b981;
            --orange:   #f59e0b;
            --red:      #ef4444;
            --text:     #e8eaf6;
            --muted:    #9ca3af;
            --radius:   12px;
            --gradient: linear-gradient(135deg, #00d4ff, #6366f1);
            --shadow:   0 4px 24px rgba(0,0,0,.4);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Segoe UI', system-ui, sans-serif;
            background: var(--bg);
            color: var(--text);
            min-height: 100vh;
        }

        /* ── Topbar ── */
        .topbar {
            background: var(--surface);
            border-bottom: 1px solid var(--border);
            padding: 0 28px;
            height: 58px;
            display: flex; align-items: center; justify-content: space-between;
            position: sticky; top: 0; z-index: 100;
        }
        .topbar-brand {
            display: flex; align-items: center; gap: 12px;
        }
        .topbar-right {
            display: flex; align-items: center; gap: 12px;
            font-size: 13px; color: var(--muted);
        }
        .user-chip {
            background: var(--surface2); border: 1px solid var(--border);
            padding: 5px 14px; border-radius: 20px; font-size: 13px;
            font-weight: 500; color: var(--text);
        }
        .logout-link {
            color: var(--red); text-decoration: none; font-size: 13px;
            font-weight: 500; padding: 6px 12px; border-radius: 7px;
            transition: background .15s;
        }
        .logout-link:hover { background: rgba(239,68,68,.1); }

        /* ── Tab bar ── */
        .tab-bar {
            background: var(--surface);
            border-bottom: 1px solid var(--border);
            padding: 0 28px;
            display: flex; gap: 4px;
        }
        .tab-link {
            padding: 14px 18px; font-size: 13.5px; font-weight: 500;
            color: var(--muted); text-decoration: none;
            border-bottom: 2px solid transparent;
            transition: all .15s;
            display: flex; align-items: center; gap: 7px;
        }
        .tab-link:hover { color: var(--text); }
        .tab-link.active { color: var(--cyan); border-bottom-color: var(--cyan); font-weight: 600; }

        /* ── Page wrapper ── */
        .page { max-width: 1080px; margin: 0 auto; padding: 28px 20px 60px; }

        /* ── Section header ── */
        .sec {
            font-size: 11px; font-weight: 700; color: var(--muted);
            letter-spacing: 1px; text-transform: uppercase;
            margin: 32px 0 14px;
            display: flex; align-items: center; gap: 10px;
        }
        .sec::after { content: ''; flex: 1; height: 1px; background: var(--border); }

        /* ── Cards ── */
        .card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            box-shadow: var(--shadow);
        }
        .card-body { padding: 22px; }
        .card-header {
            padding: 16px 22px;
            border-bottom: 1px solid var(--border);
            display: flex; align-items: center; justify-content: space-between;
        }
        .card-title { font-size: 14px; font-weight: 600; }

        /* ── Info grid (mini stat cards) ── */
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(155px, 1fr));
            gap: 12px; margin-bottom: 22px;
        }
        .ii {
            background: var(--surface2); border: 1px solid var(--border);
            border-radius: 9px; padding: 14px;
        }
        .ii-lbl {
            font-size: 10px; color: var(--muted); font-weight: 700;
            letter-spacing: .6px; text-transform: uppercase; margin-bottom: 6px;
        }
        .ii-val { font-size: 17px; font-weight: 700; }
        .ii-sub { font-size: 11.5px; color: var(--muted); margin-top: 3px; }

        /* ── Status badge ── */
        .badge {
            display: inline-flex; align-items: center; gap: 5px;
            padding: 4px 12px; border-radius: 20px;
            font-size: 12px; font-weight: 600;
        }
        .badge-active    { background: rgba(16,185,129,.12);  color: #10b981; border: 1px solid rgba(16,185,129,.3); }
        .badge-suspended { background: rgba(245,158,11,.12);  color: #f59e0b; border: 1px solid rgba(245,158,11,.3); }
        .badge-deactive  { background: rgba(239,68,68,.12);   color: #ef4444; border: 1px solid rgba(239,68,68,.3); }
        .badge-onhold    { background: rgba(156,163,175,.12); color: #9ca3af; border: 1px solid rgba(156,163,175,.3); }

        /* ── Progress bar ── */
        .pg-wrap { margin-bottom: 18px; }
        .pg-wrap:last-child { margin-bottom: 0; }
        .pg-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 7px; }
        .pg-name { font-size: 13.5px; font-weight: 600; }
        .pg-nums { font-size: 12px; color: var(--muted); }
        .pg-bg {
            height: 9px; background: var(--surface2);
            border-radius: 99px; overflow: hidden; border: 1px solid var(--border);
        }
        .pg-fill { height: 100%; border-radius: 99px; transition: width .6s ease; width: 0; }
        .fill-cyan   { background: linear-gradient(90deg, #00d4ff, #0ea5e9); }
        .fill-indigo { background: linear-gradient(90deg, #6366f1, #8b5cf6); }
        .fill-orange { background: linear-gradient(90deg, #f59e0b, #d97706); }
        .fill-red    { background: linear-gradient(90deg, #ef4444, #dc2626); }
        .pg-bot { display: flex; justify-content: space-between; font-size: 11.5px; color: var(--muted); margin-top: 5px; }

        /* ── Credit gauge ── */
        .credit-row { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; margin-bottom: 22px; }
        .credit-big { font-size: 30px; font-weight: 800; color: var(--cyan); }
        .credit-of  { font-size: 13px; color: var(--muted); margin-top: 2px; }
        .credit-pct { font-size: 13px; font-weight: 600; margin-top: 6px; }
        .credit-bar { flex: 1; min-width: 200px; }
        .bar-labels { display: flex; justify-content: space-between; font-size: 11.5px; color: var(--muted); margin-top: 5px; }

        /* ── Contract card ── */
        .ct-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); margin-bottom: 24px; }
        .ct-head {
            padding: 16px 22px; background: var(--surface2);
            border-bottom: 1px solid var(--border);
            display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px;
            border-radius: var(--radius) var(--radius) 0 0;
        }
        .ct-msisdn { font-size: 17px; font-weight: 700; }
        .ct-meta   { font-size: 12.5px; color: var(--muted); margin-top: 3px; }
        .ct-body   { padding: 20px; }

        /* ── Bills table ── */
        .tbl-wrap { overflow-x: auto; margin-bottom: 22px; }
        .tbl { width: 100%; border-collapse: collapse; font-size: 13px; }
        .tbl th {
            padding: 10px 14px; text-align: left; font-size: 10.5px; font-weight: 700;
            color: var(--indigo); letter-spacing: .7px; text-transform: uppercase;
            background: var(--surface2); border-bottom: 1px solid var(--border);
        }
        .tbl td { padding: 12px 14px; border-bottom: 1px solid var(--border); }
        .tbl tbody tr:hover { background: rgba(99,102,241,.04); }
        .tbl tbody tr:last-child td { border-bottom: none; }
        .amount { color: var(--green); font-weight: 700; }
        .chip {
            background: var(--surface2); border: 1px solid var(--border);
            padding: 3px 8px; border-radius: 6px; font-size: 11.5px;
            display: inline-block; margin: 1px 2px;
        }

        /* ── Invoice download button ── */
        .dl-btn {
            display: inline-flex; align-items: center; gap: 5px;
            padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 600;
            background: var(--gradient);
            color: #fff; text-decoration: none; transition: opacity .15s;
        }
        .dl-btn:hover { opacity: .85; }

        /* ── Invoices tab — card grid ── */
        .inv-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
        .inv-card {
            background: var(--surface); border: 1px solid var(--border);
            border-radius: var(--radius); padding: 20px;
            display: flex; flex-direction: column; gap: 12px;
        }
        .inv-head { display: flex; align-items: center; justify-content: space-between; }
        .inv-id     { font-size: 15px; font-weight: 700; }
        .inv-period { font-size: 12px; color: var(--muted); }
        .inv-amount { font-size: 24px; font-weight: 800; color: var(--cyan); }
        .inv-detail { font-size: 12px; color: var(--muted); display: flex; gap: 12px; flex-wrap: wrap; }

        /* ── Plan comparison cards ── */
        .plans-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(210px, 1fr)); gap: 14px; }
        .plan-card {
            background: var(--surface); border: 1px solid var(--border);
            border-radius: var(--radius); padding: 20px; transition: border-color .2s;
        }
        .plan-card:hover { border-color: var(--cyan); }
        .plan-card.mine  { border-color: var(--green); background: rgba(16,185,129,.04); }
        .mine-tag {
            display: inline-block; background: rgba(16,185,129,.15); color: #10b981;
            border: 1px solid rgba(16,185,129,.35); font-size: 10px; font-weight: 700;
            padding: 2px 9px; border-radius: 20px; margin-bottom: 6px;
        }
        .pc-name { font-size: 15px; font-weight: 700; margin-bottom: 4px; }
        .pc-fee  { font-size: 28px; font-weight: 800; color: var(--cyan); margin: 10px 0 16px; }
        .pc-fee span { font-size: 13px; font-weight: 500; color: var(--muted); }
        .pc-row { display: flex; justify-content: space-between; font-size: 12.5px; padding: 6px 0; border-bottom: 1px solid var(--border); }
        .pc-row:last-of-type { border-bottom: none; }
        .pc-lbl { color: var(--muted); }

        /* ── Welcome banner ── */
        .welcome {
            background: linear-gradient(135deg, rgba(99,102,241,.15), rgba(0,212,255,.08));
            border: 1px solid rgba(99,102,241,.25); border-radius: var(--radius);
            padding: 22px 26px; margin-bottom: 28px;
            display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px;
        }
        .welcome h2 { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
        .welcome p  { font-size: 13.5px; color: var(--muted); }
        .welcome-stats { display: flex; gap: 24px; }
        .ws-num { font-size: 22px; font-weight: 800; color: var(--cyan); }
        .ws-lbl { font-size: 11px; color: var(--muted); font-weight: 500; text-transform: uppercase; letter-spacing: .5px; }

        /* ── Empty state ── */
        .empty-state { text-align: center; padding: 60px 20px; color: var(--muted); }
        .empty-state .ei { font-size: 48px; margin-bottom: 14px; }

        /* ── Login page ── */
        .login-orb { position: fixed; border-radius: 50%; filter: blur(100px); opacity: .11; pointer-events: none; }
        .lo1 { width: 500px; height: 500px; background: var(--indigo); top: -160px; right: -120px; }
        .lo2 { width: 420px; height: 420px; background: var(--cyan);   bottom: -120px; left: -120px; }
        .login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .login-card {
            position: relative; z-index: 1;
            width: 100%; max-width: 420px;
            background: var(--surface); border: 1px solid var(--border);
            border-radius: 16px; padding: 40px;
            box-shadow: 0 8px 48px rgba(0,0,0,.6);
        }
        .login-logo { text-align: center; margin-bottom: 30px; }
        .login-icon {
            width: 64px; height: 64px; border-radius: 18px; margin: 0 auto 14px;
            background: var(--gradient);
            display: flex; align-items: center; justify-content: center;
        }
        .login-logo h1 { font-size: 21px; font-weight: 700; }
        .login-logo h1 .tel  { color: #fff; }
        .login-logo h1 .nova { background: var(--gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
        .login-logo p { font-size: 13px; color: var(--muted); margin-top: 5px; }
        .fg { margin-bottom: 18px; }
        label { display: block; font-size: 11px; font-weight: 600; color: var(--muted); letter-spacing: .6px; text-transform: uppercase; margin-bottom: 7px; }
        input {
            width: 100%; background: var(--surface2); border: 1px solid var(--border);
            border-radius: 8px; color: var(--text); padding: 11px 14px;
            font-size: 14px; font-family: inherit;
        }
        input:focus { outline: none; border-color: var(--cyan); box-shadow: 0 0 0 3px rgba(0,212,255,.1); }
        .login-btn {
            width: 100%; padding: 12px; margin-top: 4px;
            background: var(--gradient); color: #fff; border: none; border-radius: 8px;
            font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; transition: opacity .15s;
        }
        .login-btn:hover { opacity: .85; }
        .alert-err {
            padding: 11px 14px; border-radius: 8px; margin-bottom: 18px;
            background: rgba(239,68,68,.1); border: 1px solid rgba(239,68,68,.3);
            color: #f87171; font-size: 13px;
        }
        .divider { text-align: center; font-size: 12px; color: var(--muted); margin-top: 18px; }
        .divider a { color: var(--cyan); text-decoration: none; }
        .divider a:hover { text-decoration: underline; }

        @media (max-width: 680px) {
            .info-grid { grid-template-columns: 1fr 1fr; }
            .credit-row { flex-direction: column; align-items: flex-start; }
            .welcome { flex-direction: column; }
            .tab-link { padding: 12px 12px; font-size: 12.5px; }
        }
    """;

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN PAGE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders the full portal login page HTML.
     *
     * @param error error message to display, or null
     */
    public static String loginPage(String error) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — Telnova</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            """).append(CSS).append("</style></head><body>");

        sb.append("""
            <div class='login-orb lo1'></div>
            <div class='login-orb lo2'></div>
            <div class='login-wrap'>
              <div class='login-card'>
                <div class='login-logo'>
                  <div class='login-icon'>
            """).append(LOGO_SVG).append("""
                  </div>
                  <h1><span class='tel'>Tel</span><span class='nova'>nova</span></h1>
                  <p>Sign in to view your account and invoices</p>
                </div>
            """);

        if (error != null) {
            sb.append("<div class='alert-err'>⚠️ ").append(e(error)).append("</div>");
        }

        sb.append("""
              <form method='post'>
                <div class='fg'>
                  <label>Email Address</label>
                  <input type='email' name='email' placeholder='you@example.com'
                         autofocus autocomplete='username'>
                </div>
                <div class='fg'>
                  <label>Password</label>
                  <input type='password' name='password' placeholder='••••••••'
                         autocomplete='current-password'>
                </div>
                <button class='login-btn'>Sign In →</button>
              </form>
              <p class='divider'>Admin? <a href='../login'>Go to Admin Panel</a></p>
            </div></div>
            </body></html>
            """);

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PORTAL HEADER  (topbar + tab bar + opening <div class='page'>)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders the portal page header: full HTML head, topbar, and tab bar.
     *
     * @param userName    customer's display name (from session)
     * @param contextPath servlet context path
     * @param activeTab   one of: "overview" | "invoices" | "plans"
     */
    public static String header(String userName, String contextPath, String activeTab) {
        if (activeTab == null) activeTab = "overview";

        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — Telnova</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            """).append(CSS).append("</style></head><body>");

        // Topbar
        sb.append("<div class='topbar'>")
          .append("<div class='topbar-brand'>").append(LOGO_SVG)
          .append(LOGO_WORDMARK).append("</div>")
          .append("<div class='topbar-right'>")
          .append("<span class='user-chip'>👤 ").append(e(userName)).append("</span>")
          .append("<a href='").append(contextPath).append("/portal/logout' class='logout-link'>Sign Out</a>")
          .append("</div></div>");

        // Tab bar
        String base = contextPath + "/portal/home";
        sb.append("<div class='tab-bar'>");
        tab(sb, base,                    "overview", activeTab, "📋", "My Contracts");
        tab(sb, base + "?tab=invoices", "invoices", activeTab, "🧾", "Invoices");
        tab(sb, base + "?tab=plans",    "plans",    activeTab, "💳", "Available Plans");
        sb.append("</div>");

        // Open page wrapper
        sb.append("<div class='page'>");

        return sb.toString();
    }

    /**
     * Legacy overload kept for backward compatibility.
     * Calls the new header() with activeTab = "overview".
     */
//    public static String header(String msisdn, String userName, String contextPath) {
//        return header(userName, contextPath, "overview");
//    }

    // ─────────────────────────────────────────────────────────────────────────
    // PORTAL FOOTER
    // ─────────────────────────────────────────────────────────────────────────

    /** Closes .page, animates progress bars, closes body/html. */
    public static String footer() {
        return """
            </div>
            <script>
              document.querySelectorAll('.pg-fill').forEach(bar => {
                const w = bar.getAttribute('data-width') || '0';
                bar.style.width = '0';
                requestAnimationFrame(() => setTimeout(() => bar.style.width = w + '%', 80));
              });
            </script>
            </body></html>
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** HTML-escapes a value; returns empty string for null. */
    public static String e(Object val) {
        if (val == null) return "";
        return val.toString()
                .replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Renders a contract status badge. */
    public static String statusBadge(String status) {
        if (status == null) return "";
        return switch (status) {
            case "active"    -> "<span class='badge badge-active'>● Active</span>";
            case "suspended" -> "<span class='badge badge-suspended'>⏸ Suspended</span>";
            case "de-active" -> "<span class='badge badge-deactive'>✕ De-active</span>";
            case "on-hold"   -> "<span class='badge badge-onhold'>⏳ On Hold</span>";
            default          -> "<span class='badge'>" + e(status) + "</span>";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static void tab(StringBuilder sb, String href, String id,
                             String activeTab, String icon, String label) {
        String active = id.equals(activeTab) ? " active" : "";
        sb.append("<a href='").append(href).append("' class='tab-link").append(active).append("'>")
          .append(icon).append(" ").append(label)
          .append("</a>");
    }
}