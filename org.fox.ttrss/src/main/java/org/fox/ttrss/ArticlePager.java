package org.fox.ttrss;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;

import org.fox.ttrss.types.Article;
import org.fox.ttrss.types.ArticleList;
import org.fox.ttrss.types.Feed;
import org.fox.ttrss.util.HeadlinesRequest;

import java.util.HashMap;

import icepick.State;

public class ArticlePager extends StateSavedFragment {

	private final String TAG = "ArticlePager";
	private PagerAdapter m_adapter;
	private HeadlinesEventListener m_listener;
	@State protected Article m_article;
	@State protected ArticleList m_articles = new ArticleList(); //m_articles = Application.getInstance().m_loadedArticles;
	private OnlineActivity m_activity;
	private String m_searchQuery = "";
	@State protected Feed m_feed;
	private SharedPreferences m_prefs;
	@State protected int m_firstId = 0;
	private boolean m_refreshInProgress;
	private boolean m_lazyLoadDisabled;

	private class PagerAdapter extends FragmentStatePagerAdapter {
		
		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		private ArticleFragment m_currentFragment;

		// workaround for possible TransactionTooLarge exception on 8.0+
		// we don't need to save member state anyway, bridge takes care of it
		@Override
		public Parcelable saveState() {
			Bundle bundle = (Bundle) super.saveState();

			if (bundle != null)
				bundle.putParcelableArray("states", null); // Never maintain any states from the base class, just null it out

			return bundle;
		}

		@Override
		public Fragment getItem(int position) {
			try {
				Article article = m_articles.get(position);

				if (article != null) {
					ArticleFragment af = new ArticleFragment();
					af.initialize(article);

					return af;
				}
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public int getCount() {
			return m_articles.size();
		}

        public ArticleFragment getCurrentFragment() {
            return m_currentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
			m_currentFragment = ((ArticleFragment) object);

            super.setPrimaryItem(container, position, object);
        }

	}
		
	public void initialize(Article article, Feed feed, ArticleList articles) {
		m_article = article;
		m_feed = feed;
        m_articles = articles;
	}

	public void setSearchQuery(String searchQuery) {
		m_searchQuery = searchQuery;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {    	
		View view = inflater.inflate(R.layout.article_pager, container, false);
	
		if (savedInstanceState != null) {
			if (m_activity instanceof DetailActivity) {
				m_articles = ((DetailActivity)m_activity).m_articles;
			}
		}
		
		m_adapter = new PagerAdapter(getActivity().getSupportFragmentManager());
		
		ViewPager pager = view.findViewById(R.id.article_pager);
				
		int position = m_articles.indexOf(m_article);
		
		m_listener.onArticleSelected(m_article, false);

		pager.setAdapter(m_adapter);

		pager.setCurrentItem(position);
		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				Log.d(TAG, "onPageSelected: " + position);

				final Article article = m_articles.get(position);

				if (article != null) {
					m_article = article;

					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							m_listener.onArticleSelected(article, false);
						}
					}, 250);

					//Log.d(TAG, "Page #" + position + "/" + m_adapter.getCount());

					if (!m_refreshInProgress && !m_lazyLoadDisabled && (m_activity.isSmallScreen() || m_activity.isPortrait()) && position >= m_adapter.getCount() - 5) {
						Log.d(TAG, "loading more articles...");

						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								refresh(true);
							}
						}, 100);
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		return view;
	}
	
	@SuppressWarnings({ "serial" }) 
	protected void refresh(final boolean append) {

		if (!append) {
			m_lazyLoadDisabled = false;
		}

		m_refreshInProgress = true;

		@SuppressLint("StaticFieldLeak") HeadlinesRequest req = new HeadlinesRequest(getActivity().getApplicationContext(), m_activity, m_feed, m_articles) {
			@Override
			protected void onProgressUpdate(Integer... progress) {
				m_activity.setProgress(progress[0] / progress[1] * 10000);
			}

			@Override
			protected void onPostExecute(JsonElement result) {
				if (isDetached() || !isAdded()) return;

				if (!append) {
					ViewPager pager = getView().findViewById(R.id.article_pager);
					pager.setCurrentItem(0);

					m_articles.clear();
				}

				super.onPostExecute(result);

				m_refreshInProgress = false;

				if (result != null) {

					if (m_firstIdChanged) {
						m_lazyLoadDisabled = true;
					}

					if (m_firstIdChanged && !(m_activity instanceof DetailActivity && !m_activity.isPortrait())) {
						//m_activity.toast(R.string.headlines_row_top_changed);

						Snackbar.make(getView(), R.string.headlines_row_top_changed, Snackbar.LENGTH_LONG)
								.setAction(R.string.reload, new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										refresh(false);
									}
								}).show();
					}

					if (m_amountLoaded < Integer.valueOf(m_prefs.getString("headlines_request_size", "15"))) {
						m_lazyLoadDisabled = true;
					}

					ArticlePager.this.m_firstId = m_firstId;

					try {
						m_adapter.notifyDataSetChanged();
					} catch (BadParcelableException e) {
						if (getActivity() != null) {							
							getActivity().finish();
							return;
						}
					}
					
					if (m_article != null) {
						if (m_article.id == 0 || !m_articles.containsId(m_article.id)) {
							if (m_articles.size() > 0) {
								m_article = m_articles.get(0);
								m_listener.onArticleSelected(m_article, false);
							}
						}
					}

				} else {
					m_lazyLoadDisabled = true;

					if (m_lastError == ApiCommon.ApiError.LOGIN_FAILED) {
						m_activity.login(true);
					} else {
						m_activity.toast(getErrorMessage());
						//setLoadingStatus(getErrorMessage(), false);
					}	
				}
			}
		};
		
		final Feed feed = m_feed;
		
		final String sessionId = m_activity.getSessionId();
		int skip = 0;
		
		if (append) {
			// adaptive, all_articles, marked, published, unread
			String viewMode = m_activity.getViewMode();
			int numUnread = 0;
			int numAll = m_articles.size();
			
			for (Article a : m_articles) {
				if (a.unread) ++numUnread;
			}
			
			if ("marked".equals(viewMode)) {
				skip = numAll;
			} else if ("published".equals(viewMode)) {
				skip = numAll;
			} else if ("unread".equals(viewMode)) {
				skip = numUnread;					
			} else if (m_searchQuery != null && m_searchQuery.length() > 0) {
				skip = numAll;
			} else if ("adaptive".equals(viewMode)) {
				skip = numUnread > 0 ? numUnread : numAll;
			} else {
				skip = numAll;
			}
		}
		
		final int fskip = skip;
		
		req.setOffset(skip);

		HashMap<String,String> map = new HashMap<String,String>() {
			{
				put("op", "getHeadlines");
				put("sid", sessionId);
				put("feed_id", String.valueOf(feed.id));
                put("show_excerpt", "true");
                put("excerpt_length", String.valueOf(CommonActivity.EXCERPT_MAX_LENGTH));
				put("show_content", "true");
				put("include_attachments", "true");
				put("limit", m_prefs.getString("headlines_request_size", "15"));
				put("offset", String.valueOf(0));
				put("view_mode", m_activity.getViewMode());
				put("skip", String.valueOf(fskip));
				put("include_nested", "true");
                put("has_sandbox", "true");
				put("order_by", m_activity.getSortMode());
				
				if (feed.is_cat) put("is_cat", "true");
				
				if (m_searchQuery != null && m_searchQuery.length() != 0) {
					put("search", m_searchQuery);
					put("search_mode", "");
					put("match_on", "both");
				}

				if (m_firstId > 0) put("check_first_id", String.valueOf(m_firstId));

				if (m_activity.getApiLevel() >= 12) {
					put("include_header", "true");
				}

				if (m_prefs.getBoolean("enable_image_downsampling", false)) {
					if (m_prefs.getBoolean("always_downsample_images", false) || !m_activity.isWifiConnected()) {
						put("resize_width", String.valueOf(m_activity.getResizeWidth()));
					}
				}
			}			 
		};

        Log.d(TAG, "[AP] request more headlines, firstId=" + m_firstId);

		req.execute(map);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);		
		
		m_listener = (HeadlinesEventListener)activity;
		m_activity = (OnlineActivity)activity;
		
		m_prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();

		//if (m_adapter != null) m_adapter.notifyDataSetChanged();

		m_activity.invalidateOptionsMenu();
	}

	public Article getSelectedArticle() {
		return m_article;
	}

	public void setActiveArticle(Article article) {
		if (m_article != article) {
			m_article = article;

			int position = m_articles.indexOf(m_article);

			ViewPager pager = getView().findViewById(R.id.article_pager);
		
			pager.setCurrentItem(position);
		}
	}

	public void selectArticle(boolean next) {
		if (m_article != null) {
			int position = m_articles.indexOf(m_article);
			
			if (next) 
				position++;
			else
				position--;
			
			try {
				Article tmp = m_articles.get(position);
				
				if (tmp != null) {
					setActiveArticle(tmp);
				}
				
			} catch (IndexOutOfBoundsException e) {
				// do nothing
			}
		}		
	}

	public void notifyUpdated() {
		m_adapter.notifyDataSetChanged();
	}
}
