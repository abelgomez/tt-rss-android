package org.fox.ttrss;

import android.os.Bundle;

import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;

import org.fox.ttrss.types.Article;
import org.fox.ttrss.types.ArticleList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedHashMap;

import icepick.Icepick;

public class Application extends android.app.Application {
	private static Application m_singleton;
	
	public ArticleList tmpArticleList;
	public Article tmpArticle;

	public int m_selectedArticleId;
	public String m_sessionId;
	public int m_apiLevel;
	public LinkedHashMap<String, String> m_customSortModes = new LinkedHashMap<String, String>();

	public static Application getInstance(){
		return m_singleton;
	}
	
	@Override
	public final void onCreate() {
		super.onCreate();

		Bridge.initialize(getApplicationContext(), new SavedStateHandler() {
			@Override
			public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
				Icepick.saveInstanceState(target, state);
			}

			@Override
			public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
				Icepick.restoreInstanceState(target, state);
			}
		});

		m_singleton = this;
	}
	
	public void save(Bundle out) {
		
		out.setClassLoader(getClass().getClassLoader());
		out.putString("gs:sessionId", m_sessionId);
		out.putInt("gs:apiLevel", m_apiLevel);
		out.putInt("gs:selectedArticleId", m_selectedArticleId);
		out.putSerializable("gs:customSortTypes", m_customSortModes);
	}
	
	public void load(Bundle in) {
		if (in != null) {
			m_sessionId = in.getString("gs:sessionId");
			m_apiLevel = in.getInt("gs:apiLevel");
			m_selectedArticleId = in.getInt("gs:selectedArticleId");

			try {
				m_customSortModes = (LinkedHashMap<String, String>) in.getSerializable("gs:customSortTypes");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
				
	}
}
