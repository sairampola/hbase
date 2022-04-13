package org.apache.hadoop.hbase.util.customthreadattribute;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;

/**
 * A utility class for handling the set/get/clear operations of custom thread attributes
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class CustomThreadAttributeUtil {

  private static final Log LOG = LogFactory.getLog(CustomThreadAttributeUtil.class);

  private CustomThreadAttributeUtil(){
    throw new IllegalStateException("Utility class");
  }

  /**
   * Get all the attributes that are enabled from the current thread's context
   * @param conf
   * @return List of {@link CustomThreadAttribute}
   */
  public static List<CustomThreadAttribute> getAllAttributes(Configuration conf) {
    List<CustomThreadAttribute> attributes = new ArrayList<>();
    for (AttributeType attributeType : AttributeType.values()) {
      if (isEnabled(attributeType, conf)) {
        try {
          AttributeTypeHandler handler =
            getHandler(getImplementationClasspath(attributeType, conf));
          List<CustomThreadAttribute> attributesOfSameType = handler.getAllAttributes();
          for(CustomThreadAttribute attribute:attributesOfSameType){
            attribute.setType(attributeType);
          }
          attributes.addAll(attributesOfSameType);
        } catch (Exception exception) {
          LOG.error("An exception occurred while fetching all attributes", exception);
        }
      }
    }
    return attributes;
  }

  /**
   * Sets the attributes into current thread's context
   * @param attributes List of {@link CustomThreadAttribute}
   * @param conf
   */
  public static void setAttributes(List<CustomThreadAttribute> attributes, Configuration conf) {
    if (attributes == null || attributes.isEmpty()) return;

    for (CustomThreadAttribute attribute : attributes) {
      if (isEnabled(attribute.getType(), conf)) {
        try {
          AttributeTypeHandler handler =
            getHandler(getImplementationClasspath(attribute.getType(), conf));
          handler.setAttribute(attribute.getKey(), attribute.getValue());
        } catch (Exception exception) {
          LOG.error("An exception occurred while setting attribute " + attribute.getKey(), exception);
        }
      }
    }
  }

  /**
   * Clears the attributes from the current thread's context
   * @param attributes List of {@link CustomThreadAttribute}
   * @param conf
   */
  public static void clearAttributes(List<CustomThreadAttribute> attributes, Configuration conf) {
    if (attributes == null || attributes.isEmpty()) return;

    for (CustomThreadAttribute attribute : attributes) {
      if (isEnabled(attribute.getType(), conf)) {
        try {
          AttributeTypeHandler handler =
            getHandler(getImplementationClasspath(attribute.getType(), conf));
          handler.clearAttribute(attribute.getKey());
        } catch (Exception exception) {
          LOG.error("An exception occurred while clearing attributes", exception);
        }
      }
    }
  }

  /**
   * Get an attribute from the current thread's context
   * @param attribute {@link CustomThreadAttribute} object with key and type set
   * @param conf
   * @return {@link CustomThreadAttribute}
   */
  public static CustomThreadAttribute getAttribute(CustomThreadAttribute attribute,
    Configuration conf) {
    CustomThreadAttribute value = null;
    if (isEnabled(attribute.getType(), conf)) {
      try {
        AttributeTypeHandler handler =
          getHandler(getImplementationClasspath(attribute.getType(), conf));
        value = handler.getAttribute(attribute.getKey());
        value.setType(attribute.getType());
      } catch (Exception exception) {
        LOG.error("An exception occurred while fetching attribute " + attribute.getKey(), exception);
      }
    }
    return value;
  }

  private static Boolean isEnabled(AttributeType attributeType, Configuration conf) {
    String property = attributeType.toString() + HConstants.CUSTOM_THREAD_ATTRIBUTE_ENABLED_SUFFIX;
    return conf.getBoolean(property, HConstants.CUSTOM_THREAD_ATTRIBUTE_DEFAULT_ENABLED);
  }

  private static String getImplementationClasspath(AttributeType attributeType,
    Configuration conf) {
    String property = attributeType.toString() + HConstants.CUSTOM_THREAD_ATTRIBUTE_IMPLEMENTATION_SUFFIX;
    return conf.get(property, null);
  }

  private static AttributeTypeHandler getHandler(String classpath)
    throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
    InstantiationException, IllegalAccessException {
      Class<?> handlerClass = Class.forName(classpath);
      handlerClass.getDeclaredConstructor().setAccessible(true);
      return (AttributeTypeHandler) handlerClass.getDeclaredConstructor().newInstance();
  }
}

