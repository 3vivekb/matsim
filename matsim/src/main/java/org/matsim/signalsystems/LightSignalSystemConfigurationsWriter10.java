/* *********************************************************************** *
 * project: org.matsim.*
 * LightSignalSystemConfigurationsWriter10
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.signalsystems;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.MatsimJaxbXmlWriter;
import org.matsim.jaxb.lightsignalsystemsconfig10.ObjectFactory;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLLightSignalGroupConfigurationType;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLLightSignalSystemConfig;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLLightSignalSystemConfigurationType;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLLightSignalSystemPlanType;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLMatsimTimeAttributeType;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLPlanbasedlightSignalSystemControlInfoType;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLLightSignalGroupConfigurationType.XMLInterimTimeDropping;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLLightSignalSystemPlanType.XMLStart;
import org.matsim.jaxb.lightsignalsystemsconfig10.XMLLightSignalSystemPlanType.XMLStop;
import org.matsim.signalsystems.config.PlanBasedSignalSystemControlInfo;
import org.matsim.signalsystems.config.SignalGroupSettings;
import org.matsim.signalsystems.config.SignalSystemConfiguration;
import org.matsim.signalsystems.config.SignalSystemConfigurations;
import org.matsim.signalsystems.config.SignalSystemPlan;


/**
 * Writer for the lightSignalSystemConfiguration_v1.0.xsd file format.
 * @author dgrether
 * @deprecated use signalSystemConfigurationsWriter11 instead
 */
@Deprecated
public class LightSignalSystemConfigurationsWriter10 extends MatsimJaxbXmlWriter{

	
	private SignalSystemConfigurations blssconfs;
	private XMLLightSignalSystemConfig xmllssconfig;

	public LightSignalSystemConfigurationsWriter10(SignalSystemConfigurations basiclssconfigs) {
		this.blssconfs = basiclssconfigs;
		try {
			this.xmllssconfig = convertBasicToXml();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void write(final String filename) {
  	JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(org.matsim.jaxb.lightsignalsystemsconfig10.ObjectFactory.class);
			Marshaller m = jc.createMarshaller(); 
			super.setMarshallerProperties(MatsimSignalSystemConfigurationsReader.SIGNALSYSTEMSCONFIG10, m);
			BufferedWriter bufout = IOUtils.getBufferedWriter(filename);
			m.marshal(this.xmllssconfig, bufout); 
			bufout.close();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private XMLLightSignalSystemConfig convertBasicToXml() throws DatatypeConfigurationException {
		ObjectFactory fac = new ObjectFactory();
		XMLLightSignalSystemConfig xmllssconf = fac.createXMLLightSignalSystemConfig();
		
		for (SignalSystemConfiguration lssconf : this.blssconfs.getSignalSystemConfigurations().values()) {
			XMLLightSignalSystemConfigurationType xmllssconfiguration = fac.createXMLLightSignalSystemConfigurationType();
			xmllssconfiguration.setRefId(lssconf.getSignalSystemId().toString());
			
			if (lssconf.getControlInfo() instanceof PlanBasedSignalSystemControlInfo) {
				XMLPlanbasedlightSignalSystemControlInfoType xmlplanlsscontrolinfo = fac.createXMLPlanbasedlightSignalSystemControlInfoType();
				PlanBasedSignalSystemControlInfo pbcontrolinfo = (PlanBasedSignalSystemControlInfo) lssconf.getControlInfo();
				for (SignalSystemPlan plan : pbcontrolinfo.getPlans().values()) {
					XMLLightSignalSystemPlanType xmlplan = fac.createXMLLightSignalSystemPlanType();
					xmlplan.setId(plan.getId().toString());
					XMLStart start = new XMLStart();
					start.setDaytime(getXmlGregorianCalendar(plan.getStartTime()));
					xmlplan.setStart(start);
					
					XMLStop stop = new XMLStop();
					stop.setDaytime(getXmlGregorianCalendar(plan.getEndTime()));
					xmlplan.setStop(stop);
					
					XMLMatsimTimeAttributeType xmlct = fac.createXMLMatsimTimeAttributeType();
					if (plan.getCycleTime() != null) {
						xmlct.setSeconds(plan.getCycleTime());
						xmlplan.setCirculationTime(xmlct);
					}
					if (plan.getSynchronizationOffset() != null) {
						XMLMatsimTimeAttributeType xmlso = fac.createXMLMatsimTimeAttributeType();
						xmlso.setSeconds(plan.getSynchronizationOffset());
						xmlplan.setSyncronizationOffset(xmlso);
					}
					
					

					//write lightSignalGroupConfigurations
					for (SignalGroupSettings lsgc : plan.getGroupConfigs().values()) {
						XMLLightSignalGroupConfigurationType xmllsgc = fac.createXMLLightSignalGroupConfigurationType();
						xmllsgc.setRefId(lsgc.getReferencedSignalGroupId().toString());
						XMLLightSignalGroupConfigurationType.XMLRoughcast xmlrc = new XMLLightSignalGroupConfigurationType.XMLRoughcast();
						//FIXME change in dataformat from int to double
						xmlrc.setSec((int)lsgc.getRoughCast());
						xmllsgc.setRoughcast(xmlrc);
						
						XMLLightSignalGroupConfigurationType.XMLDropping xmldropping = new XMLLightSignalGroupConfigurationType.XMLDropping();
						xmldropping.setSec((int)lsgc.getDropping());
						xmllsgc.setDropping(xmldropping);
						if (lsgc.getInterGreenTimeDropping() != null) {
							XMLLightSignalGroupConfigurationType.XMLInterimTimeDropping xmlitd = new XMLInterimTimeDropping();
							xmlitd.setSec((int) lsgc.getInterGreenTimeDropping().doubleValue());
							xmllsgc.setInterimTimeDropping(xmlitd);
						}

						if (lsgc.getInterimGreenTimeRoughcast() != null) {
							XMLLightSignalGroupConfigurationType.XMLInterimTimeRoughcast xmlitr = new XMLLightSignalGroupConfigurationType.XMLInterimTimeRoughcast();
							xmlitr.setSec((int) lsgc.getInterimGreenTimeRoughcast().doubleValue());
							xmllsgc.setInterimTimeRoughcast(xmlitr);
						}
						
						xmlplan.getLightSignalGroupConfiguration().add(xmllsgc);
					}
					xmlplanlsscontrolinfo.getLightSignalSystemPlan().add(xmlplan);
				}
				xmllssconfiguration.setLightSignalSystemControlInfo(xmlplanlsscontrolinfo);
			}
			else {
				throw new UnsupportedOperationException("Implemented in v1.1 version of data format, please convert your files!");
			}
			xmllssconf.getLightSignalSystemConfiguration().add(xmllssconfiguration);
		}
		return xmllssconf;
	}



	private XMLGregorianCalendar getXmlGregorianCalendar(double seconds) throws DatatypeConfigurationException {
		XMLGregorianCalendar time = DatatypeFactory.newInstance().newXMLGregorianCalendar();
		int s = (int) seconds;
		int h = (s / 3600);
		s = s % 3600;
		int m = (s / 60);
		s = s % 60;
		time.setSecond(s);
		time.setMinute(m);
		time.setHour(h);
		return time;
	}

	
	
}
