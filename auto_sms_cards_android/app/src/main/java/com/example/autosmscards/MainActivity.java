package com.example.autosmscards;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int REQ_PERMS = 10;
    private static final int REQ_FILE = 20;

    LinearLayout root;
    LinearLayout content;
    LinearLayout nav;
    String activeTab = "home";
    int importAmount = 50;

    final int purple = Color.rgb(109, 75, 179);
    final int purpleLight = Color.rgb(200, 179, 255);
    final int bg = Color.rgb(17, 16, 22);
    final int card = Color.rgb(30, 27, 41);
    final int card2 = Color.rgb(40, 35, 55);
    final int text = Color.rgb(244, 241, 255);
    final int muted = Color.rgb(185, 179, 201);
    final int green = Color.rgb(66, 245, 138);
    final int orange = Color.rgb(255, 189, 89);
    final int red = Color.rgb(255, 99, 122);
    final int blue = Color.rgb(93, 168, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionsIfNeeded();
        buildLayout();
        showHome();
    }

    private void requestPermissionsIfNeeded() {
        ArrayList<String> perms = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.RECEIVE_SMS);
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.READ_SMS);
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.SEND_SMS);
        if (!perms.isEmpty()) requestPermissions(perms.toArray(new String[0]), REQ_PERMS);
    }

    private void buildLayout() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        setContentView(root);

        root.addView(topHeader(), new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(14), dp(14), dp(14));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(dp(4), dp(6), dp(4), dp(6));
        nav.setBackgroundColor(card);
        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));
        rebuildNav();
    }

    private View topHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(16), dp(24), dp(16), dp(14));
        header.setBackgroundColor(purple);

        TextView title = new TextView(this);
        title.setText("نظام الكروت");
        title.setTextColor(Color.WHITE);
        title.setTextSize(21);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        header.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView sub = new TextView(this);
        sub.setText("إرسال الكروت تلقائيًا عند استلام رسائل الإيداع");
        sub.setTextColor(Color.argb(220, 255, 255, 255));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(4), 0, 0);
        header.addView(sub, new LinearLayout.LayoutParams(-1, -2));

        return header;
    }

    private void rebuildNav() {
        nav.removeAllViews();
        nav.addView(navButton("الرئيسية", "home", "🏠", v -> showHome()));
        nav.addView(navButton("الكروت", "cards", "🎫", v -> showCards()));
        nav.addView(navButton("استيراد", "import", "⬆", v -> showImport()));
        nav.addView(navButton("السجلات", "logs", "☰", v -> showLogs()));
        nav.addView(navButton("الإعدادات", "settings", "⚙", v -> showSettings()));
    }

    private Button navButton(String label, String key, String icon, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(icon + "\n" + label);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setTextColor(key.equals(activeTab) ? purpleLight : muted);
        b.setBackground(round(key.equals(activeTab) ? Color.rgb(48, 42, 66) : card, dp(16), Color.TRANSPARENT, 0));
        b.setOnClickListener(l);
        b.setLayoutParams(new LinearLayout.LayoutParams(0, dp(58), 1));
        return b;
    }

    private void setTab(String key) {
        activeTab = key;
        rebuildNav();
    }

    private void clear() {
        content.removeAllViews();
    }

    private void addSpace(int h) {
        Space s = new Space(this);
        content.addView(s, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radius);
        if (strokeWidth > 0) gd.setStroke(strokeWidth, strokeColor);
        return gd;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView tv(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setGravity(Gravity.RIGHT);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private TextView title(String value) {
        TextView t = tv(value, 20, text, true);
        t.setPadding(0, dp(4), 0, dp(10));
        return t;
    }

    private TextView small(String value) {
        TextView t = tv(value, 13, muted, false);
        t.setLineSpacing(2, 1.1f);
        return t;
    }

    private LinearLayout cardBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(14), dp(14), dp(14));
        box.setBackground(round(card, dp(18), Color.argb(28, 255, 255, 255), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(12));
        box.setLayoutParams(lp);
        return box;
    }

    private TextView badge(String value, int color) {
        TextView b = tv(value, 12, color, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(10), dp(5), dp(10), dp(5));
        b.setBackground(round(Color.argb(28, Color.red(color), Color.green(color), Color.blue(color)), dp(20), color, dp(1)));
        return b;
    }

    private Button actionButton(String label, int bgColor, int fgColor, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(fgColor);
        b.setTypeface(null, Typeface.BOLD);
        b.setPadding(dp(8), dp(10), dp(8), dp(10));
        b.setBackground(round(bgColor, dp(16), Color.TRANSPARENT, 0));
        b.setOnClickListener(l);
        return b;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    private void addTwoStats(String title1, String val1, String title2, String val2) {
        LinearLayout r = row();
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, -2, 1);
        lp1.setMargins(0, 0, dp(6), 0);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, -2, 1);
        lp2.setMargins(dp(6), 0, 0, 0);
        r.addView(statCard(title1, val1), lp1);
        r.addView(statCard(title2, val2), lp2);
        content.addView(r);
        addSpace(12);
    }

    private View statCard(String label, String value) {
        LinearLayout box = cardBox();
        box.setGravity(Gravity.CENTER);
        TextView l = tv(label, 12, muted, false);
        l.setGravity(Gravity.CENTER);
        TextView v = tv(value, 28, text, true);
        v.setGravity(Gravity.CENTER);
        box.addView(l);
        box.addView(v);
        return box;
    }

    private int totalAvailable() {
        int total = 0;
        ArrayList<CardItem> cards = CardStore.loadCards(this);
        for (CardItem c : cards) if (!c.sold) total++;
        return total;
    }

    private int totalSold() {
        int total = 0;
        ArrayList<CardItem> cards = CardStore.loadCards(this);
        for (CardItem c : cards) if (c.sold) total++;
        return total;
    }

    private int pendingLogs() {
        int total = 0;
        for (OperationLog l : CardStore.loadLogs(this)) if (l.status.contains("معلق")) total++;
        return total;
    }

    private void showHome() {
        setTab("home");
        clear();

        LinearLayout status = cardBox();
        status.addView(tv("حالة النظام", 18, text, true));
        status.addView(small(CardStore.isAutoSendEnabled(this) ? "✅ الإرسال التلقائي مفعّل ويستقبل رسائل Jawali / Jaib / ONE Cash." : "⏸ الإرسال التلقائي متوقف حاليًا."));
        content.addView(status);

        addTwoStats("الكروت المتاحة", String.valueOf(totalAvailable()), "الكروت المباعة", String.valueOf(totalSold()));
        addTwoStats("العمليات المسجلة", String.valueOf(CardStore.loadLogs(this).size()), "العمليات المعلقة", String.valueOf(pendingLogs()));

        content.addView(title("الفئات الافتراضية"));
        for (int amount : CardStore.DEFAULT_AMOUNTS) {
            content.addView(categoryCard(amount));
        }

        LinearLayout note = cardBox();
        note.addView(tv("قاعدة الإرسال", 16, text, true));
        note.addView(small("كل مبلغ يرسل كرتًا من نفس الفئة فقط: 50→50، 100→100، 200→200. ويمكن إضافة فئات أخرى من صفحة الاستيراد."));
        content.addView(note);
    }

    private View categoryCard(int amount) {
        LinearLayout box = cardBox();

        LinearLayout top = row();
        TextView name = tv("فئة " + amount + " ريال", 17, text, true);
        TextView b = badge(String.valueOf(amount), purpleLight);
        top.addView(b, new LinearLayout.LayoutParams(-2, -2));
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(top);

        addInsideSpace(box, 8);
        box.addView(small("المتاح: " + CardStore.availableCount(this, amount) + " كرت    |    المباع: " + CardStore.soldCount(this, amount)));
        return box;
    }

    private void addInsideSpace(LinearLayout parent, int h) {
        Space s = new Space(this);
        parent.addView(s, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private void showCards() {
        setTab("cards");
        clear();
        content.addView(title("الكروت"));

        ArrayList<CardItem> cards = CardStore.loadCards(this);
        if (cards.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(tv("لا توجد كروت", 18, text, true));
            empty.addView(small("استخدم صفحة الاستيراد لإضافة كروت من ملف TXT أو إضافة يدوية."));
            content.addView(empty);
            return;
        }

        for (CardItem c : cards) {
            LinearLayout item = cardBox();

            LinearLayout top = row();
            top.addView(badge("فئة " + c.amount, purpleLight), new LinearLayout.LayoutParams(-2, -2));
            TextView state = badge(c.sold ? "مباع" : "متاح", c.sold ? orange : green);
            top.addView(state, new LinearLayout.LayoutParams(-2, -2));
            item.addView(top);

            addInsideSpace(item, 8);
            item.addView(tv(c.code, 17, text, true));
            if (c.sold) item.addView(small("أرسل إلى: " + c.buyerPhone + " | الوقت: " + c.soldAt));
            else item.addView(small("جاهز للإرسال عند وصول تحويل مطابق للفئة."));
            content.addView(item);
        }
    }

    private void showImport() {
        setTab("import");
        clear();

        content.addView(title("استيراد الكروت"));

        LinearLayout selectCard = cardBox();
        selectCard.addView(tv("اختر الفئة", 17, text, true));
        selectCard.addView(small("الفئات الافتراضية: 50، 100، 150، 200، 250، 300، 500"));
        Spinner spinner = new Spinner(this);
        String[] amounts = {"50", "100", "150", "200", "250", "300", "500"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, amounts);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                importAmount = Integer.parseInt(amounts[position]);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        addInsideSpace(selectCard, 10);
        selectCard.addView(spinner);
        content.addView(selectCard);

        LinearLayout importCard = cardBox();
        importCard.addView(tv("إضافة كروت", 17, text, true));
        importCard.addView(small("ملف TXT: كل سطر = كرت واحد. التطبيق يتجاهل المكرر والفارغ."));
        addInsideSpace(importCard, 10);
        importCard.addView(actionButton("استيراد من ملف TXT", purple, Color.WHITE, v -> openTxtFile()));
        addInsideSpace(importCard, 8);
        importCard.addView(actionButton("إضافة يدوية / لصق عدة كروت", card2, text, v -> showManualAddDialog()));
        content.addView(importCard);

        LinearLayout custom = cardBox();
        custom.addView(tv("فئة جديدة", 17, text, true));
        custom.addView(small("أضف فئة غير موجودة مثل 750 أو 1000 ثم أدخل كروتها يدويًا."));
        addInsideSpace(custom, 10);
        custom.addView(actionButton("إضافة فئة جديدة", Color.rgb(55, 45, 78), purpleLight, v -> showNewAmountDialog()));
        content.addView(custom);
    }

    private void showLogs() {
        setTab("logs");
        clear();
        content.addView(title("السجلات"));

        ArrayList<OperationLog> logs = CardStore.loadLogs(this);
        if (logs.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(tv("لا توجد عمليات", 18, text, true));
            empty.addView(small("عند وصول رسائل الإيداع ستظهر العمليات هنا."));
            content.addView(empty);
            return;
        }

        for (OperationLog l : logs) {
            LinearLayout item = cardBox();
            int color = l.status.contains("تم") ? green : (l.status.contains("معلق") ? orange : red);
            item.addView(badge(l.status, color));
            addInsideSpace(item, 8);
            item.addView(tv("المبلغ: " + l.amount + " ريال", 17, text, true));
            item.addView(small("المصدر: " + l.sender));
            item.addView(small("الرقم: " + (l.customerPhone.isEmpty() ? "-" : l.customerPhone)));
            item.addView(small("الكرت: " + (l.cardCode.isEmpty() ? "-" : l.cardCode)));
            item.addView(small("الوقت: " + l.createdAt));
            item.addView(small("ملاحظة: " + l.message));
            content.addView(item);
        }
    }

    private void showSettings() {
        setTab("settings");
        clear();
        content.addView(title("الإعدادات"));

        LinearLayout auto = cardBox();
        Switch sw = new Switch(this);
        sw.setText("تشغيل الإرسال التلقائي");
        sw.setTextColor(text);
        sw.setTextSize(17);
        sw.setTypeface(null, Typeface.BOLD);
        sw.setChecked(CardStore.isAutoSendEnabled(this));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> CardStore.setAutoSendEnabled(this, isChecked));
        auto.addView(sw);
        auto.addView(small("عند التفعيل سيقرأ التطبيق رسائل الجهات الموثوقة ويرسل الكرت تلقائيًا."));
        content.addView(auto);

        LinearLayout names = cardBox();
        names.addView(tv("الأسماء الموثوقة لوَن كاش", 17, text, true));
        names.addView(small("في ONE Cash تتم المطابقة على الاسم الثلاثي ثم الإرسال إلى الرقم المحفوظ."));
        addInsideSpace(names, 10);
        names.addView(actionButton("إدارة الأسماء الموثوقة", purple, Color.WHITE, v -> showTrustedNames()));
        content.addView(names);

        LinearLayout providers = cardBox();
        providers.addView(tv("الجهات الموثوقة", 17, text, true));
        providers.addView(small("Jawali / جوالي\nJaib / جيب\nONE Cash / ون كاش"));
        content.addView(providers);

        LinearLayout danger = cardBox();
        danger.addView(tv("حذف البيانات", 17, text, true));
        danger.addView(small("يحذف الكروت والسجلات والأسماء الموثوقة من هذا الجهاز."));
        addInsideSpace(danger, 10);
        danger.addView(actionButton("حذف كل البيانات", Color.rgb(82, 30, 42), Color.WHITE, v -> new AlertDialog.Builder(this)
                .setTitle("تأكيد")
                .setMessage("هل تريد حذف الكروت والسجلات والأسماء الموثوقة؟")
                .setPositiveButton("نعم", (d, w) -> {
                    CardStore.clearAll(this);
                    Toast.makeText(this, "تم الحذف", Toast.LENGTH_SHORT).show();
                    showHome();
                })
                .setNegativeButton("لا", null)
                .show()));
        content.addView(danger);
    }

    private void showTrustedNames() {
        setTab("settings");
        clear();
        content.addView(title("الأسماء الموثوقة - ONE Cash"));

        LinearLayout add = cardBox();
        add.addView(tv("إضافة اسم موثوق", 17, text, true));
        add.addView(small("أدخل الاسم كما يظهر في رسالة ون كاش، وسيركز النظام على الاسم الثلاثي."));
        addInsideSpace(add, 10);
        add.addView(actionButton("إضافة اسم موثوق", purple, Color.WHITE, v -> showAddTrustedContactDialog()));
        content.addView(add);

        ArrayList<TrustedContact> list = CardStore.loadTrustedContacts(this);
        if (list.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(tv("لا توجد أسماء محفوظة", 18, text, true));
            empty.addView(small("أضف الاسم والرقم، وبعدها أي تحويل جديد بنفس الاسم الثلاثي سيُعتمد تلقائيًا."));
            content.addView(empty);
            return;
        }

        for (TrustedContact c : list) {
            LinearLayout item = cardBox();
            item.addView(tv(c.name, 18, text, true));
            item.addView(small("الاسم الثلاثي المعتمد: " + c.tripleName));
            item.addView(small("رقم الإرسال: " + c.phone));
            addInsideSpace(item, 8);
            item.addView(actionButton("حذف الاسم", Color.rgb(82, 30, 42), Color.WHITE, v -> {
                CardStore.deleteTrustedContact(this, c.id);
                showTrustedNames();
            }));
            content.addView(item);
        }
    }

    private void showAddTrustedContactDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(10), dp(10), dp(10));

        final EditText name = new EditText(this);
        name.setHint("الاسم كما يظهر في رسالة ون كاش");
        name.setInputType(InputType.TYPE_CLASS_TEXT);

        final EditText phone = new EditText(this);
        phone.setHint("رقم الهاتف لإرسال الكرت");
        phone.setInputType(InputType.TYPE_CLASS_PHONE);

        box.addView(name);
        box.addView(phone);

        new AlertDialog.Builder(this)
                .setTitle("إضافة اسم موثوق")
                .setView(box)
                .setPositiveButton("حفظ", (dialog, which) -> {
                    String n = name.getText().toString().trim();
                    String p = phone.getText().toString().trim();
                    if (n.isEmpty() || p.isEmpty()) {
                        Toast.makeText(this, "الاسم والرقم مطلوبان", Toast.LENGTH_LONG).show();
                        return;
                    }
                    CardStore.addTrustedContact(this, n, p);
                    Toast.makeText(this, "تم حفظ الاسم الثلاثي: " + NameUtils.tripleName(n), Toast.LENGTH_LONG).show();
                    showTrustedNames();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void openTxtFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_FILE);
    }

    private void showManualAddDialog() {
        final EditText input = new EditText(this);
        input.setMinLines(6);
        input.setGravity(Gravity.TOP | Gravity.RIGHT);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("كل سطر = كرت واحد");

        new AlertDialog.Builder(this)
                .setTitle("إضافة كروت فئة " + importAmount)
                .setView(input)
                .setPositiveButton("حفظ", (dialog, which) -> {
                    ArrayList<String> lines = new ArrayList<>();
                    for (String line : input.getText().toString().split("\\r?\\n")) lines.add(line);
                    int added = CardStore.importCards(this, importAmount, lines, "manual");
                    Toast.makeText(this, "تمت إضافة " + added + " كرت", Toast.LENGTH_LONG).show();
                    showHome();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showNewAmountDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("مثال: 750");

        new AlertDialog.Builder(this)
                .setTitle("إضافة فئة جديدة")
                .setView(input)
                .setPositiveButton("اعتماد الفئة", (dialog, which) -> {
                    int amount = 0;
                    try { amount = Integer.parseInt(input.getText().toString().trim()); } catch (Exception ignored) {}
                    if (amount <= 0) {
                        Toast.makeText(this, "أدخل فئة صحيحة", Toast.LENGTH_LONG).show();
                        return;
                    }
                    importAmount = amount;
                    Toast.makeText(this, "تم اختيار فئة " + amount + ". استخدم الإضافة اليدوية لإدخال الكروت.", Toast.LENGTH_LONG).show();
                    showManualAddDialog();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            importFromUri(uri);
        }
    }

    private void importFromUri(Uri uri) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
            reader.close();

            int added = CardStore.importCards(this, importAmount, lines, getFileName(uri));
            Toast.makeText(this, "تم استيراد " + added + " كرت", Toast.LENGTH_LONG).show();
            showHome();
        } catch (Exception e) {
            Toast.makeText(this, "فشل قراءة الملف: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = "txt";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
                cursor.close();
            }
        } catch (Exception ignored) {}
        return result;
    }
}
