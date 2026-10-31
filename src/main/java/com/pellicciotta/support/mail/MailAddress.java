// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.utils.*;

/**
 *  A mail address consisting of a local and domain part.
 *  <p>
 *  Names and validation based on http://en.wikipedia.org/wiki/Email_address.</p>
**/
public class MailAddress
{
  // <editor-fold desc="Constants">

  public static final int MAX_LOCAL_PART_LENGTH  =  64;
  public static final int MAX_DOMAIN_PART_LENGTH = 253;
  public static final int MAX_ADDRESS_LENGTH     = 254;

  // </editor-fold>
  // <editor-fold desc="Validation Methods">

  /**
   *  Check whether the provided string is a valid email address.
   *  Note that this is a simple form of validation that doesn't enforce all rules as defined in the RFCs.
   *  Only lengths are checked and neither parts should include the @-sign.
   *
   *  TODO: Full validation according to RFC
   *
   *  @param address Address to be checked.
   *
   *  @return True if valid, false otherwise.
  **/
  public static boolean isValid(String address) {
    return isValid(address, null);
  }

  /**
   *  Check whether the provided string is a valid email address
   *  and if not, and errors is not null: provide more info about why not.
   *
   *  @param address Address to be checked.
   *  @param errors If not null, error descriptions - if any - will be added to this list.
   *
   *  @return True if valid, false otherwise.
  **/
  public static boolean isValid(String address, List<String> errors) {
    // Check overall length
    if ((address == null) || (address.length() < 3)) {
      if (errors != null) {
        errors.add("Address must be at least 3 characters long");
      }
      return false;
    }
    int i = address.indexOf("@");
    if (i == -1) {
      if (errors != null) {
        errors.add("Address has no @-sign");
      }
      return false;
    }
    if (address.length() > MAX_ADDRESS_LENGTH) {
      if (errors != null) {
        errors.add("Address length is beyond max. length of " + MAX_ADDRESS_LENGTH);
      }
      return false;
    }
    // Split in parts
    String localPart = address.substring(0, i);
    String domainPart = address.substring(i + 1);
    // Check domain part
    if (domainPart.length() < 2) {
      if (errors != null) {
        errors.add("Domain part must be at least 2 characters long");
      }
      return false;
    }
    if (domainPart.length() > MAX_DOMAIN_PART_LENGTH) {
      if (errors != null) {
        errors.add("Domain part length is beyond max. length of " + MAX_DOMAIN_PART_LENGTH);
      }
      return false;
    }
    if (domainPart.contains("@")) {
      if (errors != null) {
        errors.add("Domain part contains the @-sign");
      }
      return false;
    }
    // Check local part
    if (localPart.length() < 2) {
      if (errors != null) {
        errors.add("Local part must be at least 2 characters long");
      }
      return false;
    }
    if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
      if (errors != null) {
        errors.add("Local part length is beyond max. length of " + MAX_LOCAL_PART_LENGTH);
      }
      return false;
    }
    if (localPart.contains("@")) {
      if (errors != null) {
        errors.add("Local part contains the @-sign");
      }
      return false;
    }
    return true;
  }

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   *  Create an email address from the provided string.
   *
   *  @param address Should have the form <pre>{local-part}@{domain-part}</pre>.
   *
   *  @exception IllegalArgumentException If address is not valid as per isValid
  **/
  public MailAddress(String address) {
    Contract.isNotNull("address", address);
    List<String> errors = new ArrayList<String>();
    if (!isValid(address, errors)) {
      throw new IllegalArgumentException("Invalid address: " + CollectionUtils.toString(errors));
    }
    int i = address.indexOf("@");
    this.localPart = address.substring(0, i);
    this.domainPart = address.substring(i + 1);
    this.alias = null;
  }

  /**
   *  Create an email address with the provided local (i.e. user) and domain (i.e. host) parts.
   *
   *  @param localPart Local name portion of the address. Must not be null.
   *  @param domainPart Host part. Can be null.
   *
   *  @exception IllegalArgumentException If either user or host contains invalid chars or user is null.
  **/
  public MailAddress(String localPart, String domainPart) {
    Contract.isNotNull("localPart", localPart);
    List<String> errors = new ArrayList<String>();
    String address = localPart;
    if (domainPart != null) {
      address = localPart + "@" + domainPart;
    }
    if (!isValid(address, errors)) {
      throw new IllegalArgumentException("Invalid address: " + CollectionUtils.toString(errors));
    }
    this.localPart = localPart;
    this.domainPart = domainPart;
    this.alias = null;
  }

  /**
   *  Create an email address with the provided local (i.e. user) and domain (i.e. host) parts.
   *
   *  @param localPart Local name portion of the address. Must not be null.
   *  @param domainPart Host part. Can be null.
   *  @param alias Alias name. Can be null.
   *
   *  @exception IllegalArgumentException If either user or host contains invalid chars or user is null.
   **/
  public MailAddress(String localPart, String domainPart, String alias) {
    Contract.isNotNull("localPart", localPart);
    List<String> errors = new ArrayList<String>();
    String address = localPart;
    if (domainPart != null) {
      address = localPart + "@" + domainPart;
    }
    if (!isValid(address, errors)) {
      throw new IllegalArgumentException("Invalid address: " + CollectionUtils.toString(errors));
    }
    this.localPart = localPart;
    this.domainPart = domainPart;
    this.alias = alias;
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final String localPart;
  private final String domainPart;
  private final String alias;

  /**
   *  @return Local part of an address.
  **/
  public String getLocalPart() {
    return this.localPart;
  }

  /**
   *  @return Domain part of the address.
   **/
  public String getDomainPart() {
    return this.domainPart;
  }

  /**
   *  @return Alias for the address.
  **/
  public String getAlias() {
    return this.alias;
  }

  // </editor-fold>
  // <editor-fold desc="Conversion Operators">

  public InternetAddress toInternetAddress() throws AddressException {
    try {
      if (this.alias != null) {
        InternetAddress ia = new InternetAddress(this.toString(), this.alias);
        return ia;
      }
    }
    catch (UnsupportedEncodingException uee) { }
    return new InternetAddress(this.toString());
  }

  // </editor-fold>
  // <editor-fold desc="Textual Representation">

  /**
   *  @return Either of the following:
   *          <pre>
   *            ${alias} &lt;${local-part}@${domain-part}&gt;
   *            &lt;${local-part}@${domain-part}&gt;
   *          </pre>
   **/
  @Override
  public String toString() {
    if (this.alias != null) {
      return String.format("%s <%s@%s>", this.alias, this.localPart, this.domainPart);
    }
    else {
      return String.format("<%s@%s>", this.localPart, this.domainPart);
    }
  }

  // </editor-fold>
}
