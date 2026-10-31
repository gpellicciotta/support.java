// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.utils.*;
import org.w3c.dom.*;

/**
 * This class takes responsibility for configuring an object instance based on a provided XML configuration.
 * <p>
 * It uses reflection and name matching to determine which XML element corresponds to which property,
 * then invoking the property with a correctly converted value type.</p>
 *
 * <h3>Example 1: when there is no configure method</h3>
 * <p>Given an XML configuration fragment that looks as follows:</p>
 * <pre>
 *   &lt;task name="com.pellicciotta.test.TestTask&gt;
 *     &lt;report-type&gt;html&lt;/report-type&gt;
 *     &lt;report-type&gt;csv&lt;/report-type&gt;
 *     &lt;continue-on-error&gt;no&lt;continue-on-error&gt;
 *     &lt;style-config&gt;
 *       &lt;file-type-pattern&gt;COBOL&lt;/file-type-pattern&gt;
 *       &lt;file-name-pattern&gt;.*[.]cbl&lt;/file-name-pattern&gt;
 *       &lt;style-name&gt;classic&lt;/style-name&gt;
 *     &lt;/style-config&gt;
 *     &lt;style-config&gt;
 *       &lt;file-type-pattern&gt;NATURAL.*&lt;/file-type-pattern&gt;
 *       &lt;style-name&gt;highlight&lt;/style-name&gt;
 *       &lt;custom-filter class=&quot;com.pellicciotta.demo.VerySpecialFilter&quot;/&gt;
 *     &lt;/style-config&gt;
 *     &lt;default-font-size&gt;12&lt;/default-font-size&gt;
 *   &lt;/task&gt;
 * </pre>
 * And following classes:
 * <pre>
 * enum ReportTypes { HTML, CSV }; // Report types
 *
 * class StyleConfig {
 *   public StyleConfig() { ... } // Default constructor
 *
 *   public void setFileTypePattern(String fileTypeRegex) { ... }
 *   public void setFileNamePattern(String fileNameRegex) { ... }
 *   public void setStyleName(String name) { ... }
 * }
 *
 * class TestTask extends Task {
 *   public TestTask() { ... } // Default constructor
 *
 *   public void setReportType(ReportTypes type) { ... } // Set type of report to be created
 *
 *   public void setContinueOnError(boolean cont) { ... } // Whether to go on when generation fails
 *
 *   public void addStyleConfig(StyleConfig config) { ... } // Add another piece of style-configuration
 *
 *   public void setDefaultFontSize(int size) { ... }
 * }
 * </pre>
 * an {@link XmlBasedObjectConfigurator} class object could be invoked as follows:
 * <pre>
 *   XmlBasedObjectConfigurator cf = new XmlBasedObjectConfigurator();
 *   Task task = ... code that instantiates an object of the TestTask class ...
 *   Element xmlConfig = ... code that selects the &lt;task-name&gt; element sub-tree from the XML doc ...
 *   cf.configure(task, xmlConfig);
 * </pre>
 * and the effect would be equivalent to the following series of statements:
 * <pre>
 *   task.setReportType(ReportTypes.HTML);    // Where HTML is selected based on the text 'html' as a case-insensitive match
 *                                            // ignoring special chars between the XML text and the possible enum values
 *   task.setReportType(ReportTypes.CSV);     // Multiple setXXX with same XXX will be called in sequence, whereby the XML values will
 *                                            // be provided in their original order (and in this case the last value will survive,
 *                                            // but with addXXX methods all would be collected)
 *   task.setContinueOnError(Boolean.FALSE);
 *   StyleConfig sc1 = new StyleConfig();     // Since the setStyleConfig expects a param of type StyleConfig, create that first
 *   sc1.setFileTypePattern("COBOL");
 *   sc1.setFileNamePattern(".*[.]cbl");
 *   sc1.setStyleName("classic");
 *   task.addStyleConfig(sc1);
 *   StyleConfig sc2 = new StyleConfig();     // Since the setStyleConfig expects a param of type StyleConfig, create that first
 *   sc2.setFileTypePattern("NATURAL.*");
 *   sc2.setStyleName("highlight");
 *   VerySpecialFilter vsf = new VerySpecialFilter();  // The special 'class' attribute, if specified, will be used to determine the type of class to create; it must be a sub-type of the parameter specified in setCustomFilter though.
 *   sc2.setCustomFilter(vsf);
 *   task.addStyleConfig(sc2);
 *   task.setDefaultFontSize(12);
 * </pre>
 *
 * <p>
 * <h3>Example 2: when there is a configure method</h3>
 * <p>With the same XML fragment and the same classes except for the TestTask class that now looks as follows:
 * <pre>
 * class TestTask extends Task {
 *   public TestTask() { ... } // Default constructor
 *
 *   public void configure(Element xmlConfig);
 *
 *   public void setReportType(ReportTypes type) { ... } // Set type of report to be created
 *
 *   public void setContinueOnError(boolean cont) { ... } // Whether to go on when generation fails
 *
 *   public void addStyleConfig(StyleConfig config) { ... } // Add another piece of style-configuration
 *
 *   public void setDefaultFontSize(int size) { ... }
 * }
 * </pre>
 * The effect of configuration will be limited to invoking the TestTask.configure method, which can then do whatever it wants:
 * <pre>
 *   task.configure(xmlConfig);
 * </pre>
 */
public class XmlBasedObjectConfigurator
{
  // <editor-fold desc="Construction">

  /**
   * Default constructor.
   */
  public XmlBasedObjectConfigurator() {
    this.errors = new ArrayList<>();
    this.classLoader = XmlBasedObjectConfigurator.class.getClassLoader();
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private List<Exception> errors;

  /**
   * @return Errors that have occurred during the last configure run. Empty list if there were no errors or no previous configure
   *         call.
   */
  public List<Exception> getErrors() {
    return this.errors;
  }

  // </editor-fold>
  // <editor-fold desc="Configuration Implementation">

  private ClassLoader classLoader;

  /**
   * Try to configure the given object as follows:
   * <ol>
   *   <li>If a target.configure(Element) method exists: invoke that</li>
   *
   *   <li>Otherwise try to find a property setter for each child element of config:
   *       <ol>
   *         <li>The XML element name must match (irrespective of case and non-alphanumeric chars) the method name
   *             (irrespective of 'set' or 'add' prefix)</li>
   *
   *         <li>The property setter must have one argument only</li>
   *
   *         <li>The XML element value must be convertible to the argument type.<br>
   *             Currently the following parameter types are supported:
   *             <dl>
   *               <dn><b>Element:</b></dn>
   *                 <dt>the XML element, literally</dt>
   *
   *               <dn><b>String:</b></dn>
   *                 <dt>the XML text, after expansion with {@link StringConversionUtils#toExpandedString(String)}</dt>
   *
   *               <dn><b>char or Character:</b></dn>
   *                 <dt>first char of string will be used, no warning given</dt>
   *
   *               <dn><b>int, long, Integer, Long:</b></dn>
   *                 <dt>only if XML text can be converted to an int/long via {@link StringConversionUtils#toInt(String)} or {@link StringConversionUtils#toLong(String)}</dt>
   *
   *               <dn><b>boolean, Boolean:</b></dn>
   *                 <dt>true or false according to {@link StringConversionUtils#toBoolean(String)}</dt>
   *
   *               <dn><b>Enum:</b></dn>
   *                 <dt>the enum value whose string representation (irrespective of case and non-alphanumeric chars) matches the XML text, according to {@link StringConversionUtils#toEnum(Class,String)}</dt>
   *
   *               <dn><b>List:</b></dn>
   *                 <dt>the list of String's of all XML elements with the same name</dt>
   *
   *               <dn><b>Any other class with a default constructor and <code>configure(Element)</code> method</b></dn>
   *                 <dt>instantiate an object of this class and apply this <code>configure</code> method to this object, with the child-XML element as configuration root</dt>
   *             </dl>
   *         </li>
   *       </ol>
   *   <li> For all XML elements with the same name: repeatedly call the property setter, in order of the XML elements:
   *        for setXXX methods it is assumed the last value will be remembered, while <code>addXXX</code> methods are assumed to have cumulative behavior.</li>
   * </ol>
   * Notes:
   * <ul>
   *   <li>Any attributes on XML elements will be ignored.</li>
   *   <li>When the object's class has overloaded <code>setXXX</code> or <code>addXXX</code> methods, configuration will fail.</li>
   *   <li>Only <code>setXXX</code> and <code>addXXX</code> and <code>configure</code> methods declared in the object's class will be considered, but no derived methods.</li>
   * </ul>
   *
   * @param target The object to be configured
   * @param config The XML configuration
   *
   * @return True on success, false if configuration failed, partly or completely.
   *         More details can then be found in the Errors property.
   */
  public boolean configure(Object target, Element config) {
    this.errors.clear();
    ClassLoader clsl = this.classLoader;
    try {
      this.classLoader = target.getClass().getClassLoader();
      this.configureObject(target, config);
    }
    catch (Exception e) {
      this.errors.add(e);
    }
    finally {
      this.classLoader = clsl;
    }
    return this.errors.isEmpty();
  }

  private void configureObject(Object object, Element config) throws Exception
  {
    Method[] methods = object.getClass().getDeclaredMethods();
    // First check whether the object has a configure(Element) method and if so, call it (since it's the general fallback mechanism):
    for (Method m : methods) {
      if ("configure".equals(m.getName())) {
        Class<?>[] params = m.getParameterTypes();
        if ((params.length == 1) && params[0].isAssignableFrom(Element.class)) {
          m.invoke(object, config);
          return;
        }
      }
    }
    // If no configure method, check that each XML child element has a corresponding setXXX or addXXX method.
    Map<String, Method> propSetters = new HashMap<>();
    List<Pair<String, List<Element>>> orderedElementsPerName = this.getChildElements(config);
    for (Pair<String, List<Element>> nameWithElements : orderedElementsPerName) {
      String elName = nameWithElements.getFirst();
      String propertyName = elName.toLowerCase().replaceAll("[^a-z0-9]", "");
      Method propSetter = this.getMatchingSetter(object, propertyName);
      propSetters.put(elName, propSetter);
    }
    // If we get this far: start configuring the object
    for (Pair<String, List<Element>> nameWithElements : orderedElementsPerName) {
      String elName = nameWithElements.getFirst();
      Method propSetter = propSetters.get(elName);
      Class<?> paramClass = propSetter.getParameterTypes()[0];
      List<Element> xmlValues = nameWithElements.getSecond();
      try {
        if (paramClass.isAssignableFrom(List.class)) {
          List<String> listVal = this.convertValuesToStringList(xmlValues);
          propSetter.invoke(object, listVal);
        }
        else {
          for (Element xmlValue : xmlValues) {
            Object value = this.convertValueToType(paramClass, elName, xmlValue);
            propSetter.invoke(object, value);
          }
        }
      }
      catch (Exception e) {
        this.errors.add(new ConfigurationException(elName, xmlValues, e, "Failed to configure property '%s': %s", elName, e));
      }
    }
  }

  /**
   *  @param values XML elements
   *  @return List of strings, each string being the text of the corresponding XML element.
   */
  private List<String> convertValuesToStringList(List<Element> values) {
    assert values != null;
    List<String> strValues = new ArrayList<>();
    for (Element propertyValue : values) {
      String strVal = XmlUtils.getText(propertyValue);
      strVal = StringConversionUtils.toExpandedString(strVal);
      strValues.add(strVal);
    }
    return strValues;
  }

  /**
   * Try to convert the XML value to the given type.
   *
   * @param targetType The type to convert to.
   * @param value The value to be converted. Should not be null.
   *
   * @return A converted value of a type assignable to the targetType or null if no conversion was possible
   */
  private Object convertValueToType(Class<?> targetType, String configElementName, Element value) throws NumberFormatException, Exception {
    // XML Element:
    if (targetType.isAssignableFrom(Element.class)) {
      return value;
    }
    String propertyStrVal = XmlUtils.getText(value);
    // String:
    if (targetType.isAssignableFrom(String.class)) {
      propertyStrVal = StringConversionUtils.toExpandedString(propertyStrVal);
      return propertyStrVal;
    }
    // Boolean:
    if (targetType.isAssignableFrom(Boolean.class) || (targetType == boolean.class)) {
      return StringConversionUtils.toBoolean(propertyStrVal);
    }
    // Enum:
    if (targetType.isEnum()) {
      return StringConversionUtils.toEnum((Class<? extends Enum>)targetType, propertyStrVal);
    }
    // Long:
    if (targetType.isAssignableFrom(Long.class) || (targetType == long.class)) {
      return StringConversionUtils.toLong(propertyStrVal);
    }
    // Int:
    if (targetType.isAssignableFrom(Integer.class) || (targetType == int.class)) {
      return StringConversionUtils.toInt(propertyStrVal);
    }
    // Char:
    if (targetType.isAssignableFrom(Character.class) || (targetType == char.class)) {
      return Character.valueOf(propertyStrVal.charAt(0));
    }

    // TODO: check whether targetType has class.valueOf(String) method
    // TODO: check whether targetType has .ctor(String) constructor
    // TODO: check whether there is a registered converter<String, TargetType>

    // Class with default constructor and ...
    String className = targetType.getSimpleName().toLowerCase().replaceAll("[^a-z0-9]", "");
    Constructor<?> defCon = null;
    // ... XML element has the special 'class' attribute: create object of indicated class
    if (value.hasAttribute("class")) {
      String implClassName = value.getAttribute("class");
      Class<?> implClass = this.classLoader.loadClass(implClassName);
      defCon = implClass.getConstructor();
    }
    // ... type-name matches, ignoring non-printable-chars and capitalization: create object of targetType
    String paramElemName = value.getNodeName().toLowerCase().replaceAll("[^a-z0-9]", "");
    if (className.equals(paramElemName)) {
      defCon = targetType.getConstructor();
    }
    if (defCon != null) {
      Object obj = defCon.newInstance();
      if (XmlUtils.getChildElements(value).length > 0) {
        this.configureObject(obj, value);
      }
      return obj;
    }
    throw new ConfigurationException(configElementName, value, "Cannot convert value '%s' to type '%s': not implemented", propertyStrVal, targetType);
  }

  /**
   * Retrieve all unique child element tag names in order.
   *
   * @param parentElement The element whose child elements should be searched.
   *
   * @return A list of pairs: the key will be the XML child element name, the value the list of XML elements with this name, in the original order.
   *         The list as a whole is also ordered so that the first XML element name found, will also be the first key in the list.
   */
  private List<Pair<String, List<Element>>> getChildElements(Element parentElement) {
    assert parentElement != null;

    Map<String, List<Element>> elementsPerName = new HashMap<>();
    List<String> nameOrder = new ArrayList<>();
    NodeList children = parentElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        String tagName = node.getNodeName();
        if (!elementsPerName.containsKey(tagName)) {
          elementsPerName.put(tagName, new ArrayList<Element>());
          nameOrder.add(tagName);
        }
        elementsPerName.get(tagName).add((Element) node);
      }
    }
    List<Pair<String, List<Element>>> orderedElementsPerName = new ArrayList<>();
    for (String name : nameOrder) {
      orderedElementsPerName.add(Pair.of(name, elementsPerName.get(name)));
    }
    return orderedElementsPerName;
  }

  /**
   * Retrieve a unique and matching setXXX or addXXX method that accepts 1 parameter.
   *
   * @param obj The object whose class should declare the method to be found
   * @param propertyName The name of the property (the XXX part) the method should correspond with.
   *
   * @return The unique and matching setXXX or addXXX method found in obj's class.
   *
   * @throws ConfigurationException If no unique (none or more than one) method could be found.
   */
  private Method getMatchingSetter(Object obj, String propertyName) throws ConfigurationException {
    assert obj != null;
    assert propertyName != null;

    // Get all method that could match
    List<Method> propSetters = new ArrayList<>();
    for (Method m : obj.getClass().getDeclaredMethods()) {
      if ((m.getName().startsWith("set") || m.getName().startsWith("add")) && (m.getParameterTypes().length == 1)
          && (m.getName().substring(3).equalsIgnoreCase(propertyName))) {
        propSetters.add(m);
      }
    }
    // Now check there is only one
    if (propSetters.size() != 1) {
      throw new ConfigurationException(propertyName, null, "No unique property setter method found for property '%s' in class '%s': found %s matching methods.", propertyName, obj.getClass().getName(), propSetters.size());
    }
    return propSetters.get(0);
  }
}
