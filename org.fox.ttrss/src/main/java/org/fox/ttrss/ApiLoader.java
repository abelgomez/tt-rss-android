package org.fox.ttrss;

import android.content.Context;

import com.google.gson.JsonElement;

import org.fox.ttrss.ApiCommon.ApiError;

import java.util.HashMap;

import androidx.loader.content.AsyncTaskLoader;

public class ApiLoader extends AsyncTaskLoader<JsonElement> implements ApiCommon.ApiCaller {
	private final String TAG = this.getClass().getSimpleName();

	private int m_responseCode = 0;
	protected String m_responseMessage;
	private int m_apiStatusCode = 0;

	private Context m_context;
	private String m_lastErrorMessage;
	private ApiError m_lastError;
	private HashMap<String,String> m_params;
	private JsonElement m_data;

	ApiLoader(Context context, HashMap<String, String> params) {
		super(context);

		m_context = context;
		m_lastError = ApiError.NO_ERROR;
		m_params = params;
	}

	@Override
	protected void onStartLoading() {
		if (m_data != null) {
			deliverResult(m_data);
		} else {
			forceLoad();
		}
	}

	@Override
	public void deliverResult(JsonElement data) {
		m_data = data;

		super.deliverResult(data);
	}

	public int getErrorMessage() {
		return ApiCommon.getErrorMessage(m_lastError);
	}

	ApiError getLastError() {
		return m_lastError;
	}

	String getLastErrorMessage() {
		return m_lastErrorMessage;
	}

	@Override
	public JsonElement loadInBackground() {
		return ApiCommon.performRequest(m_context, m_params, this);
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
