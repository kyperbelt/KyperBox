package com.kyperbox.dic;

import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.umisc.StringUtils;

public class Localizer {

	private Localization default_localization;
	private Localization current_localization;
	private ObjectMap<String, Localization> localizations;

	public Localizer() {
		localizations = new ObjectMap<String, Localization>();
		addLocalization(LocalizationConstants.ENG);
		this.default_localization = getLocalization(LocalizationConstants.ENG);
		this.current_localization = this.default_localization;
	}

	public void setCurrentLocalization(String localization) {
		this.current_localization = getLocalization(localization);
	}

	public Localization getDefaultLocalization() {
		return default_localization;
	}

	public Localization getLocalization(String local) {
		if (localizations.containsKey(local)) {
			return localizations.get(local);
		}
		return default_localization;
	}

	/**
	 * add a new localization to the localizer
	 * 
	 * @param localization
	 */
	public void addLocalization(String localization) {
		if (!localizations.containsKey(localization)) {

			Localization l = new Localization(localization);
			localizations.put(localization, l);

		}
	}

	/**
	 * add a new key and string pair to the default localization of the localizer
	 * 
	 * @param key
	 * @param string
	 */
	public void add(String key, String string) {
		default_localization.add(key, string);
	}

	/**
	 * add a key string pair to the specified localization of the localizer. Will
	 * throw an error if no such localization exists
	 * 
	 * @param local
	 * @param key
	 * @param string
	 */
	public void add(String local, String key, String string) {
		Localization l = localizations.get(local);
		if(l==null ) 
			throw new IllegalArgumentException(StringUtils.format(LocalizationConstants.LOCALIZATION_UNDEFINED,local));
		if(!local.equals(default_localization.local) && default_localization.get(key) == null)
			throw new IllegalArgumentException(StringUtils.format(LocalizationConstants.DEFAULT_NOT_AVAILABLE, key));
		l.add(key, string);
		
	}
	
	/**
	 * get the string from the current localization
	 * @param key
	 */
	public String get(String key) {
		String s = current_localization.get(key);
		if(s == null) {
			String ds = default_localization.get(key);
			if(ds == null) {
				throw new IllegalArgumentException(StringUtils.format(LocalizationConstants.DEFAULT_NOT_AVAILABLE, key));
			}else {
				throw new IllegalArgumentException(StringUtils.format(LocalizationConstants.UNLOCALIZED, key,ds));
			}
		}
		return s;
	}

}
