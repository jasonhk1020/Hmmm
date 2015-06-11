package com.humdinger.hmmm;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collection;

public abstract class CardStackAdapter extends BaseCardStackAdapter {
	private final Context mContext;

	/**
	 * Lock used to modify the content of {@link #mData}. Any write operation
	 * performed on the deque should be synchronized on this lock.
	 */
	private final Object mLock = new Object();
	private ArrayList<CardModel> mData;

	public CardStackAdapter(Context context) {
		mContext = context;
		mData = new ArrayList<CardModel>();
	}

	public CardStackAdapter(Context context, Collection<? extends CardModel> items) {
		mContext = context;
		mData = new ArrayList<CardModel>(items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FrameLayout wrapper = (FrameLayout) convertView;
		FrameLayout innerWrapper;
		View cardView;
		View convertedCardView;
		if (wrapper == null) {
			wrapper = new FrameLayout(mContext);
			wrapper.setBackgroundResource(R.drawable.card_bg);
			if (shouldFillCardBackground()) {
				innerWrapper = new FrameLayout(mContext);
				innerWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.card_bg));
				wrapper.addView(innerWrapper);
			} else {
				innerWrapper = wrapper;
			}
			cardView = getCardView(position, getCardModel(position), null, parent);
			innerWrapper.addView(cardView);
		} else {
			if (shouldFillCardBackground()) {
				innerWrapper = (FrameLayout) wrapper.getChildAt(0);
			} else {
				innerWrapper = wrapper;
			}
			cardView = innerWrapper.getChildAt(0);
			convertedCardView = getCardView(position, getCardModel(position), cardView, parent);
			if (convertedCardView != cardView) {
				wrapper.removeView(cardView);
				wrapper.addView(convertedCardView);
			}
		}

		return wrapper;
	}

	protected abstract View getCardView(int position, CardModel model, View convertView, ViewGroup parent);

	public boolean shouldFillCardBackground() {
		return true;
	}

	public void add(CardModel item) {
		synchronized (mLock) {
			mData.add(item);
		}
		notifyDataSetChanged();
	}


    public void remove(CardModel item) {
        synchronized (mLock) {
            mData.remove(item);
        }
        notifyDataSetChanged();
    }

    public void remove(String uid) {
        synchronized (mLock) {
            boolean found = false;
            CardModel removeCard = null;
            for (CardModel i : mData) {
                if (i.getUid() != null) {
                    if (i.getUid().equals(uid)) {
                        found = true;
                        removeCard = i;
                        break;
                    }
                }
            }
            if (found) {
                mData.remove(removeCard);
            }
        }
        notifyDataSetChanged();
    }

    public void moveToTop(CardModel item) {
        synchronized (mLock) {
            //remove item
            mData.remove(item);

            //add item back to the top or beginning of list
            mData.add(0, item);

        }
        notifyDataSetChanged();
    }

	public CardModel pop() {
		CardModel model;
		synchronized (mLock) {
			model = mData.remove(mData.size() - 1);
		}
		notifyDataSetChanged();
		return model;
	}

	@Override
	public Object getItem(int position) {
		return getCardModel(position);
	}

	public CardModel getCardModel(int position) {
		synchronized (mLock) {
			return mData.get(position);
		}
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	public Context getContext() {
		return mContext;
	}

    public boolean exists(CardModel item) {
        boolean found = false;
        for (CardModel i : mData) {
            if (i.getUid() != null) {
                if (i.getUid().equals(item.getUid())) {
                    found = true;
                }
            }
        }
        return found;
    }

    public boolean exists(String uid) {
        boolean found = false;
        for (CardModel i : mData) {
            if (i.getUid() != null) {
                if (i.getUid().equals(uid)) {
                    found = true;
                }
            }
        }
        return found;
    }

    public void update(CardModel item) {
        synchronized (mLock) {
            for (int i = 0 ; i < mData.size(); i++) {
                if(mData.get(i).getUid().equals(item.getUid())) {
                    mData.set(i, item);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }
}
