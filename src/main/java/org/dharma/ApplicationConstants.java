package org.dharma;

public class ApplicationConstants {
	
	// Generic constants
	public static final String CREATED_BY = "createdBy";
	
	
	// Event related constants
	public static final String EVENT_ID = "eventId";
	public static final int EVENT_ID_DELTA = 1;
	public static final String EVENT_NAME = "name";
	public static final String EVENT_DESC = "description";
	public static final String EVENT_LOCATION = "location";
	public static final String EVENT_GEO_LOCATION = "geoLocation";
	public static final String EVENT_DATE_TIME = "dateTime";
	public static final String EVENT_URL = "url";
	public static final String EVENT_PHOTO = "photo";
	public static final String EVENT_PUBLIC_FLAG = "isPublic";
	public static final String EVENT_SCORE_INDEX = "events";
	public static final String EVENT_NAME_INDEX = "event:name:";
	public static final String EVENT_LOCATION_INDEX = "event:location:";
	public static final String EVENT_GEOLOCATION_INDEX = "event:geoLocation:";
	public static final String EVENT_IS_PUBLIC_INDEX = "event:isPublic:";
	public static final String EVENT_DATE_TIME_INDEX = "event:date.time";
	public static final String EVENT_NOT_FOUND_EXCEPTION = "Event could not be found";
	
	// User related constants
	public static final String USER_ID = "userId";
	public static final int USER_ID_DELTA = 1;
	public static final String USER_SCORE_INDEX = "users";
	public static final String USER_FIRST_NAME = "firstName";
	public static final String USER_LAST_NAME = "lastName";
	public static final String USER_EMAIL = "email";
	public static final String USER_PHONE = "phone";
	public static final String USER_PASSWORD = "password";
	public static final String USER_NOT_FOUND_EXCEPTION = "User could not be found";
	
	// User Event Registration
	public static final String REGISTRATION_ID = "registrationId";
	public static final int REGISTRATION_ID_DELTA = 1;
	public static final String REGISTRATION_SCORE_INDEX = "registrations";
	public static final String REGISTRATION_NOT_FOUND_EXCEPTION = "Registration could not be found";
	public static final String USER_REGISTRATION_INDEX = "registration:user:";
	public static final String EVENT_REGISTRATION_INDEX = "registration:event:";
	
	
}
