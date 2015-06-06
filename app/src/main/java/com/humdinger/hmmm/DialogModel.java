/**
 * AndTinder v0.1 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 *
 * TAndTinder is a native library for Android that provide a
 * Tinder card like effect. A card can be constructed using an
 * image and displayed with animation effects, dismiss-to-like
 * and dismiss-to-unlike, and use different sorting mechanisms.
 *
 * AndTinder is compatible with API Level 13 and upwards
 *
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */

package com.humdinger.hmmm;

public class DialogModel {

	private String uid;
    private String matchUid;



    public DialogModel(String uid, String matchUid) {
		this.uid = uid;
        this.matchUid = matchUid;
	}

    public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

    public String getMatchUid() {
        return matchUid;
    }

    public void setMatchUid(String matchUid) {
        this.matchUid = matchUid;
    }

}
