/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.telecom.billing.util;

/**
 *
 * @author Ali
 */
public class CustomerLayout {

    private static final String CSS = """
        :root {
            --bg:       #0d1117;
            --surface:  #161b22;
            --surface2: #21262d;
            --border:   #30363d;
            --accent:   #2ea44f;
            --accent2:  #58a6ff;
            --warn:     #d29922;
            --danger:   #f85149;
            --text:     #e6edf3;
            --muted:    #8b949e;
            --radius:   12px;
            --shadow:   0 4px 24px rgba(0,0,0,.4);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Inter', system-ui, sans-serif;
            background: var(--bg);
            color: var(--text);
            min-height: 100vh;
        }

        /* ── Topbar ── */
        .topbar {
            background: var(--surface);
            border-bottom: 1px solid var(--border);
            padding: 0 32px;
            height: 58px;
            display: flex; align-items: center; justify-content: space-between;
            position: sticky; top: 0; z-index: 100;
        }
        .topbar-brand {
            display: flex; align-items: center; gap: 10px;
            font-size: 16px; font-weight: 700;
        }
        .brand-icon {
            width: 34px; height: 34px; border-radius: 9px;
            background: linear-gradient(135deg, #2ea44f, #58a6ff);
            display: flex; align-items: center; justify-content: center;
            font-size: 16px;
        }
        .topbar-right {
            display: flex; align-items: center; gap: 16px;
            font-size: 13.5px; color: var(--muted);
        }
        .msisdn-tag {
            background: var(--surface2); border: 1px solid var(--border);
            padding: 5px 12px; border-radius: 20px; font-size: 13px;
            font-weight: 500; color: var(--text);
        }
        .logout-link {
            color: var(--danger); text-decoration: none; font-size: 13px;
            font-weight: 500; padding: 5px 12px; border-radius: 6px;
            transition: background .15s;
        }
        .logout-link:hover { background: rgba(248,81,73,.1); }

        /* ── Page wrapper ── */
        .page { max-width: 1100px; margin: 0 auto; padding: 32px 24px; }

        /* ── Section header ── */
        .section-title {
            font-size: 13px; font-weight: 600; color: var(--muted);
            letter-spacing: .8px; text-transform: uppercase;
            margin-bottom: 14px; margin-top: 32px;
            display: flex; align-items: center; gap: 8px;
        }
        .section-title::after {
            content: ''; flex: 1; height: 1px; background: var(--border);
        }

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

        /* ── Grid layouts ── */
        .grid-2 { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
        .grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
        .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
        @media (max-width: 800px) {
            .grid-2, .grid-3, .grid-4 { grid-template-columns: 1fr; }
        }

        /* ── Stat mini cards (account overview) ── */
        .mini-stat {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            padding: 18px 20px;
        }
        .mini-label { font-size: 11.5px; color: var(--muted); font-weight: 500;
                      letter-spacing: .4px; margin-bottom: 8px; }
        .mini-value { font-size: 22px; font-weight: 700; }
        .mini-sub   { font-size: 12px; color: var(--muted); margin-top: 4px; }

        /* ── Status badge ── */
        .badge {
            display: inline-flex; align-items: center; gap: 5px;
            padding: 4px 11px; border-radius: 20px;
            font-size: 12px; font-weight: 600;
        }
        .badge-active   { background: rgba(46,164,79,.15);   color: #3fb950; border: 1px solid rgba(46,164,79,.3); }
        .badge-suspended{ background: rgba(210,153,34,.15);  color: #d29922; border: 1px solid rgba(210,153,34,.3); }
        .badge-deactive { background: rgba(248,81,73,.15);   color: #f85149; border: 1px solid rgba(248,81,73,.3); }
        .badge-onhold   { background: rgba(139,148,158,.15); color: #8b949e; border: 1px solid rgba(139,148,158,.3); }

        /* ── Progress bar ── */
        .progress-wrap { margin-bottom: 20px; }
        .progress-header {
            display: flex; justify-content: space-between; align-items: center;
            margin-bottom: 8px;
        }
        .progress-name { font-size: 13.5px; font-weight: 600; }
        .progress-nums { font-size: 12px; color: var(--muted); }
        .progress-bar-bg {
            height: 8px; background: var(--surface2);
            border-radius: 99px; overflow: hidden;
            border: 1px solid var(--border);
        }
        .progress-bar-fill {
            height: 100%; border-radius: 99px;
            transition: width .5s ease;
        }
        .fill-voice { background: linear-gradient(90deg, #a371f7, #7c5cbf); }
        .fill-data  { background: linear-gradient(90deg, #58a6ff, #1f6feb); }
        .fill-sms   { background: linear-gradient(90deg, #d29922, #bf8700); }
        .fill-warn  { background: linear-gradient(90deg, #f85149, #da3633); }
        .progress-footer {
            display: flex; justify-content: space-between;
            font-size: 11.5px; color: var(--muted); margin-top: 5px;
        }

        /* ── Credit gauge ── */
        .credit-row {
            display: flex; align-items: center; gap: 16px; margin-bottom: 10px;
        }
        .credit-amounts { flex: 1; }
        .credit-big { font-size: 28px; font-weight: 700; color: var(--accent2); }
        .credit-of  { font-size: 13px; color: var(--muted); margin-top: 2px; }

        /* ── Plan info grid ── */
        .plan-detail-grid {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
            gap: 12px; margin-top: 16px;
        }
        .plan-detail-item {
            background: var(--surface2); border: 1px solid var(--border);
            border-radius: 8px; padding: 14px;
        }
        .pd-label { font-size: 11px; color: var(--muted); font-weight: 500;
                    letter-spacing: .4px; text-transform: uppercase; margin-bottom: 6px; }
        .pd-value { font-size: 18px; font-weight: 700; }

        /* ── Bills table ── */
        .bills-table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
        .bills-table th {
            padding: 10px 14px; text-align: left; font-size: 11px;
            font-weight: 600; color: var(--muted); letter-spacing: .7px;
            text-transform: uppercase; background: var(--surface2);
            border-bottom: 1px solid var(--border);
        }
        .bills-table td {
            padding: 13px 14px; border-bottom: 1px solid var(--border);
        }
        .bills-table tbody tr:hover { background: rgba(255,255,255,.02); }
        .bills-table tbody tr:last-child td { border-bottom: none; }

        /* ── Plan comparison cards ── */
        .plan-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; }
        .plan-card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            padding: 20px;
            transition: border-color .2s;
        }
        .plan-card:hover { border-color: var(--accent2); }
        .plan-card.current-plan {
            border-color: var(--accent);
            background: rgba(46,164,79,.05);
        }
        .plan-card-name { font-size: 15px; font-weight: 700; margin-bottom: 6px; }
        .plan-card-fee  {
            font-size: 26px; font-weight: 800; color: var(--accent2);
            margin-bottom: 14px;
        }
        .plan-card-fee span { font-size: 14px; font-weight: 500; color: var(--muted); }
        .plan-card-row {
            display: flex; justify-content: space-between; align-items: center;
            font-size: 12.5px; padding: 5px 0;
            border-bottom: 1px solid var(--border);
        }
        .plan-card-row:last-of-type { border-bottom: none; }
        .plan-card-row .lbl { color: var(--muted); }
        .current-badge {
            display: inline-block; background: rgba(46,164,79,.2);
            color: #3fb950; border: 1px solid rgba(46,164,79,.4);
            font-size: 10.5px; font-weight: 600; padding: 2px 8px;
            border-radius: 20px; letter-spacing: .4px; margin-bottom: 8px;
        }

        /* ── Empty state ── */
        .empty { text-align: center; padding: 40px 20px; color: var(--muted); }
        .empty .ei { font-size: 36px; margin-bottom: 10px; }

        /* ── Login page ── */
        .login-wrap {
            min-height: 100vh; display: flex;
            align-items: center; justify-content: center;
            background: var(--bg);
        }
        .login-card {
            width: 100%; max-width: 420px;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 16px; padding: 40px;
            box-shadow: 0 8px 48px rgba(0,0,0,.6);
            position: relative; z-index: 1;
        }
        .login-orb {
            position: fixed; border-radius: 50%; filter: blur(100px); opacity: .12;
            pointer-events: none;
        }
        .lo1 { width: 450px; height: 450px; background: #2ea44f; top: -150px; right: -100px; }
        .lo2 { width: 380px; height: 380px; background: #58a6ff; bottom: -100px; left: -100px; }
        .login-logo { text-align: center; margin-bottom: 30px; }
        .login-icon {
            width: 60px; height: 60px; border-radius: 16px; margin: 0 auto 14px;
            background: linear-gradient(135deg, #2ea44f, #58a6ff);
            display: flex; align-items: center; justify-content: center; font-size: 28px;
        }
        .login-logo h1 { font-size: 20px; font-weight: 700; }
        .login-logo p  { font-size: 13px; color: var(--muted); margin-top: 5px; }
        .fg { margin-bottom: 18px; }
        label { display: block; font-size: 11px; font-weight: 600; color: var(--muted);
                letter-spacing: .6px; text-transform: uppercase; margin-bottom: 7px; }
        input {
            width: 100%; background: var(--surface2); border: 1px solid var(--border);
            border-radius: 8px; color: var(--text); padding: 11px 14px;
            font-size: 14px; font-family: inherit;
        }
        input:focus { outline: none; border-color: var(--accent2);
            box-shadow: 0 0 0 3px rgba(88,166,255,.12); }
        .login-btn {
            width: 100%; padding: 12px; margin-top: 4px;
            background: linear-gradient(135deg, #2ea44f, #3fb950);
            color: #fff; border: none; border-radius: 8px;
            font-size: 14px; font-weight: 600; cursor: pointer;
            font-family: inherit; transition: opacity .15s;
        }
        .login-btn:hover { opacity: .85; }
        .alert-err {
            padding: 11px 14px; border-radius: 8px; margin-bottom: 18px;
            background: rgba(248,81,73,.1); border: 1px solid rgba(248,81,73,.3);
            color: #f85149; font-size: 13px;
        }
        .divider { text-align: center; font-size: 12px; color: var(--muted); margin-top: 18px; }
        .divider a { color: var(--accent2); text-decoration: none; }
        .divider a:hover { text-decoration: underline; }
    """;

    // ── Login page ──────────────────────────────────────────────────────────
    public static String loginPage(String error) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — TelecomBill</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            """).append(CSS).append("</style></head><body>");

        sb.append("""
            <div class='login-orb lo1'></div>
            <div class='login-orb lo2'></div>
            <div class='login-wrap'>
              <div class='login-card'>
                <div class='login-logo'>
                  <div class='login-icon'>📱</div>
                  <h1>My Account</h1>
                  <p>Sign in with your phone number and password</p>
                </div>
            """);

        if (error != null)
            sb.append("<div class='alert-err'>⚠️ ").append(e(error)).append("</div>");

        sb.append("""
              <form method='post'>
                <div class='fg'>
                  <label>Phone Number (MSISDN)</label>
                  <input type='text' name='msisdn' placeholder='+201012345678' autofocus autocomplete='username'>
                </div>
                <div class='fg'>
                  <label>Password</label>
                  <input type='password' name='password' placeholder='••••••••' autocomplete='current-password'>
                </div>
                <button class='login-btn'>Sign In →</button>
              </form>
              <p class='divider'>Admin? <a href='../login'>Go to Admin Panel</a></p>
            </div></div>
            </body></html>
            """);
        return sb.toString();
    }

    // ── Dashboard shell: header ──────────────────────────────────────────────
    public static String header(String msisdn, String userName, String contextPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <!DOCTYPE html><html lang='en'><head>
            <meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>
            <title>My Account — TelecomBill</title>
            <link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>
            <style>
            """).append(CSS).append("</style></head><body>");

        sb.append("<div class='topbar'>")
          .append("<div class='topbar-brand'><div class='brand-icon'>📡</div>TelecomBill</div>")
          .append("<div class='topbar-right'>")
          .append("<span style='color:var(--text);font-weight:500;'>").append(e(userName)).append("</span>")
          .append("<span class='msisdn-tag'>📱 ").append(e(msisdn)).append("</span>")
          .append("<a href='").append(contextPath).append("/portal/logout' class='logout-link'>Sign Out</a>")
          .append("</div></div>")
          .append("<div class='page'>");

        return sb.toString();
    }

    // ── Footer ───────────────────────────────────────────────────────────────
    public static String footer() {
        return """
            </div>
            <script>
              // Animate progress bars on load
              document.querySelectorAll('.progress-bar-fill').forEach(bar => {
                const w = bar.getAttribute('data-width');
                bar.style.width = '0';
                setTimeout(() => bar.style.width = w + '%', 100);
              });
            </script>
            </body></html>
            """;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    public static String e(Object val) {
        if (val == null) return "";
        return val.toString()
            .replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;");
    }

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
}
