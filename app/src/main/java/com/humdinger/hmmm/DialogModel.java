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
    private String matchUsername;
    private CharSequence matchInfo;
    private String matchDescription;
    private String matchPhotoUrl;

    public DialogModel(String uid, String matchUid, String matchUsername, CharSequence matchInfo, String matchDescription, String matchPhotoUrl) {
		this.uid = uid;
        this.matchUid = matchUid;
        this.matchUsername = matchUsername;
        this.matchInfo = matchInfo;
        this.matchDescription = matchDescription;
        this.matchPhotoUrl = matchPhotoUrl;
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

    public String getMatchUsername() {
        return matchUsername;
    }

    public void setMatchUsername(String matchUsername) {
        this.matchUsername = matchUsername;
    }

    public CharSequence getMatchInfo() {
        return matchInfo;
    }

    public void setMatchInfo(CharSequence matchInfo) {
        this.matchInfo = matchInfo;
    }

    public String getMatchDescription() {
        return matchDescription;
    }

    public void setMatchDescription(String matchDescription) {
        this.matchDescription = matchDescription;
    }

    public String getMatchPhotoUrl() {
        return matchPhotoUrl;
    }

    public void setMatchPhotoUrl(String matchPhotoUrl) {
        this.matchPhotoUrl = matchPhotoUrl;
    }

}
