package org.fox.ttrss;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.JsonElement;

import java.util.HashMap;

import static org.fox.ttrss.ApiCommon.ApiError;

public class ApiRequest extends AsyncTask<HashMap<String,String>, Integer, JsonElement> implements ApiCommon.ApiCaller {
	private final String TAG = this.getClass().getSimpleName();

	private boolean m_transportDebugging = false;
	private int m_responseCode = 0;
	private int m_apiStatusCode = 0;

	private Context m_context;
	protected String m_lastErrorMessage;

	protected ApiError m_lastError;

	public ApiRequest(Context context) {
		m_context = context;
		m_lastError = ApiError.NO_ERROR;
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("unchecked")
	public void execute(HashMap<String,String> map) {
		super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map);
	}

	public int getErrorMessage() {
		return ApiCommon.getErrorMessage(m_lastError);
	}

	@Override
	protected JsonElement doInBackground(HashMap<String, String>... params) {
		return ApiCommon.performRequest(m_context, params[0], this);
	}

	@Override
	public void setStatusCode(int statusCode) {
		m_apiStatusCode = statusCode;
	}

	@Override
	public void setLastError(ApiError lastError) {
		m_lastError = lastError;
	}

	@Override
	public void setLastErrorMessage(String message) {
		m_lastErrorMessage = message;
	}
}
