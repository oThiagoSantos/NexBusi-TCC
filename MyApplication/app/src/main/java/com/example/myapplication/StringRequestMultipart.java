package com.example.myapplication;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

public abstract class StringRequestMultipart extends Request<String> {

    private final Response.Listener<String> listener;
    private final Response.ErrorListener errorListener;
    private final String boundary;
    private final String LINE_FEED = "\r\n";

    public StringRequestMultipart(int method, String url,
                                  Response.Listener<String> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
        this.boundary = UUID.randomUUID().toString();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            Map<String, String> params = getParams();
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    bos.write(("--" + boundary + LINE_FEED).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_FEED).getBytes());
                    bos.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes());
                    bos.write(LINE_FEED.getBytes());
                    bos.write(entry.getValue().getBytes());
                    bos.write(LINE_FEED.getBytes());
                }
            }

            Map<String, DataPart> data = getByteData();
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    DataPart part = entry.getValue();
                    bos.write(("--" + boundary + LINE_FEED).getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + part.getFileName() + "\"" + LINE_FEED).getBytes());
                    bos.write(("Content-Type: " + part.getType() + LINE_FEED).getBytes());
                    bos.write(LINE_FEED.getBytes());
                    bos.write(part.getContent());
                    bos.write(LINE_FEED.getBytes());
                }
            }

            bos.write(("--" + boundary + "--" + LINE_FEED).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthFailureError e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        errorListener.onErrorResponse(error);
    }

    /** Método abstrato para adicionar os dados do tipo arquivo (byte[]) */
    protected abstract Map<String, DataPart> getByteData();

    /** Classe interna para representar cada arquivo */
    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content) {
            this(fileName, content, "application/octet-stream");
        }

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
