// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import com.pellicciotta.support.codecs.StandardStringCodecs;
import com.pellicciotta.support.config.ListSetting;
import com.pellicciotta.support.config.StandaloneListSetting;
import com.pellicciotta.support.utils.CollectionUtils;
import com.pellicciotta.support.utils.XmlUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ListSettingTests {
  @Test
  public void testListSettingUsage() {
    ListSetting<String> stringListSetting = new StandaloneListSetting<String>(String.class, "string", "A list of strings",
        CollectionUtils.asList("Foo", "Bar", "Baz"), StandardStringCodecs.STRING);

    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(3, stringListSetting.getValueOrDefault().size());
    Assertions.assertFalse(stringListSetting.isValueSet());
    stringListSetting.setValue(CollectionUtils.asList("#1", "#2", "#3", "#4"));
    Assertions.assertTrue(stringListSetting.isValueSet());
    Assertions.assertEquals(4, stringListSetting.getValue().size());
    stringListSetting.addElementValue("five");
    stringListSetting.addElementValue("six");
    Assertions.assertEquals(6, stringListSetting.getValue().size());
    Assertions.assertEquals("five", stringListSetting.getValue().get(4));
  }

  @Test
  public void testEmptyListSettingLoadFromProps() {
    ListSetting<String> stringListSetting = new StandaloneListSetting<String>(String.class, "string", "A list of strings",
        CollectionUtils.asList("Foo", "Bar", "Baz"), StandardStringCodecs.STRING);


    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(3, stringListSetting.getValueOrDefault().size());
    Assertions.assertFalse(stringListSetting.isValueSet());

    // If no settings in properties map: just ignore totally:
    Map<String, String> props = new HashMap<>();
    props.put("nostring.1", "the first");

    // Load:
    stringListSetting.loadFrom(props);

    // Check no changes:
    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(3, stringListSetting.getValueOrDefault().size());
    Assertions.assertFalse(stringListSetting.isValueSet());
  }

  @Test
  public void testEmptyListSettingLoadFromXml() throws IOException {
    ListSetting<String> stringListSetting = new StandaloneListSetting<String>(String.class, "string", "A list of strings",
        CollectionUtils.asList("Foo", "Bar", "Baz"), StandardStringCodecs.STRING);

    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(3, stringListSetting.getValueOrDefault().size());
    Assertions.assertFalse(stringListSetting.isValueSet());

    Document xmlDoc = XmlUtils.createDocument("<config>" +
        "<other-setting>nothing</other-setting>" +
        "</config>");

    // Load:
    stringListSetting.loadFrom(xmlDoc.getDocumentElement());

    // Check no changes:
    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(3, stringListSetting.getValueOrDefault().size());
    Assertions.assertFalse(stringListSetting.isValueSet());
  }

  @Test
  public void testListSettingLoadFromXml() throws IOException {
    ListSetting<String> stringListSetting = new StandaloneListSetting<String>(String.class, "string", "A list of strings",
        CollectionUtils.asList("Foo", "Bar", "Baz"), StandardStringCodecs.STRING);

    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(3, stringListSetting.getValueOrDefault().size());
    Assertions.assertFalse(stringListSetting.isValueSet());

    Document xmlDoc = XmlUtils.createDocument("<config>" +
        "<other-setting>nothing</other-setting>" +
        "<string>foobarandbazcombined</string>"  +
        "</config>");

    // Load:
    stringListSetting.loadFrom(xmlDoc.getDocumentElement());

    // Check changes:
    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(1, stringListSetting.getValueOrDefault().size());
    Assertions.assertEquals("foobarandbazcombined", stringListSetting.getValue().get(0));
    Assertions.assertTrue(stringListSetting.isValueSet());
  }

  @Test
  public void testListSettingLoadFromProps() {
    ListSetting<String> stringListSetting = new StandaloneListSetting<String>(String.class, "string", "A list of strings",
        CollectionUtils.asList("Foo", "Bar", "Baz"), StandardStringCodecs.STRING);

    Map<String, String> props = new HashMap<>();
    props.put("string.1", "the first");
    props.put("string.2", "the second");
    props.put("string.3", "the third");
    props.put("string.4", "the fourth");
    props.put("string.5", "the fifth");

    Assertions.assertEquals(String.class, stringListSetting.getElementType());
    Assertions.assertEquals(3, stringListSetting.getValueOrDefault().size());
    Assertions.assertFalse(stringListSetting.isValueSet());

    // Load:
    stringListSetting.loadFrom(props);

    // Check changes:
    Assertions.assertTrue(stringListSetting.isValueSet());
    Assertions.assertEquals(5, stringListSetting.getValue().size());
    Assertions.assertEquals("the fifth", stringListSetting.getValue().get(4));
  }

  @Test
  public void testListSettingSaveToProps() {
    ListSetting<String> stringListSetting = new StandaloneListSetting<String>(String.class, "string", "A list of strings",
        CollectionUtils.asList("Foo", "Bar", "Baz"), StandardStringCodecs.STRING);

    Map<String, String> props = new HashMap<>();

    stringListSetting.saveTo(props, false);
    Assertions.assertEquals(0, props.size());

    stringListSetting.saveTo(props, true);
    Assertions.assertEquals(3, props.size());
    Assertions.assertEquals("Foo", props.get("string.1"));
    Assertions.assertEquals("Bar", props.get("string.2"));
    Assertions.assertEquals("Baz", props.get("string.3"));
  }
}
