package com.demo.ussddemo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

public class UssdService extends AccessibilityService {
    public static final String TAG ="USSDINTERACT";
    public static int lastWid;

    public void appendLog(String text) {
        Log.d(TAG, text);
        File logFile = new File(getExternalFilesDir(null), "log.txt");
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true))) {
            buf.newLine();
            buf.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date())).append(":");
            buf.newLine();
            buf.append(text);
            buf.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dfs(AccessibilityNodeInfo u, StringBuilder buffer, String prefix, String childrenPrefix) {
        if (u == null) return;  // must!!
        CharSequence nodetext = u.getText();
        buffer.append(prefix).append(nodetext != null ? nodetext.toString().replaceAll("\n", Matcher.quoteReplacement("\\n")) : "null").append('\n');
        int childCnt = u.getChildCount();
        for (int i = 0; i < childCnt - 1; i++) {
            dfs(u.getChild(i), buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
        }
        if (childCnt > 0)
            dfs(u.getChild(childCnt - 1), buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
    }

    public String treeToString(AccessibilityNodeInfo root) {
        StringBuilder buffer = new StringBuilder(100);
        dfs(root, buffer, "", "");
        return buffer.toString();
    }

    public String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // I have noticed that event.getSource tends to return null in UssD popups when screen was off. Hence I am keeping screen off.

//        appendLog("onAccessibilityEvent, class: " + event.getClassName() + ", type: " + AccessibilityEvent.eventTypeToString(event.getEventType()));
        if (!event.getClassName().equals("android.app.AlertDialog")) return; // device specific

        String text = event.getText().toString();

        if (!text.startsWith("[Today")) return;

        int wid = event.getWindowId();
        appendLog("window id: " + wid + ", last window id: " + lastWid + ", windows: " + getWindows());

        AccessibilityNodeInfo root = getRootInActiveWindow(), source = event.getSource();
        AccessibilityNodeInfo nodeInfo = source == null ? root : source;

        appendLog("source: " + source);
        appendLog("root: " + root);

        // the input you wanna give and the button you wanna press after input
        String input = "0", button = "Cancel";

        try {
            AccessibilityNodeInfo nodeInput = nodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            Bundle bundle = new Bundle();
            bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, input);
            nodeInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);
            nodeInput.refresh();
            appendLog("input " + input);
        } catch (Exception e) {
            appendLog("could not input :(\n" + stackTraceToString(e));
        }

        try {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(button);
            if (list.isEmpty()) throw new Exception("couldn't find button "+button);
            for (AccessibilityNodeInfo node : list) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                appendLog("push " + button);
            }
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            appendLog("could not push :(\n" + stackTraceToString(e));
            performGlobalAction(GLOBAL_ACTION_HOME);
            if (wid != lastWid) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
            }
        }

        appendLog("read:\n" + text + "\n");
        lastWid = wid;
    }

    @Override
    public void onInterrupt() {
        appendLog("onInterrupt... :(");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        appendLog("onUnbind... :(");
        return super.onUnbind(intent);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        appendLog("onServiceConnected");
        lastWid = 0;

        try {
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.flags = AccessibilityServiceInfo.DEFAULT;
            info.packageNames = new String[]{"com.android.phone"};
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            setServiceInfo(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
