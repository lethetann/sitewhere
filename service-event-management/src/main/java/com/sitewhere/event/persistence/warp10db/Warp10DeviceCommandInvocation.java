/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.event.persistence.warp10db;

import com.sitewhere.rest.model.device.event.DeviceCommandInvocation;
import com.sitewhere.spi.device.event.CommandInitiator;
import com.sitewhere.spi.device.event.CommandTarget;
import com.sitewhere.spi.device.event.IDeviceCommandInvocation;
import com.sitewhere.warp10.Warp10Converter;
import com.sitewhere.warp10.rest.GTSInput;
import com.sitewhere.warp10.rest.GTSOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Warp10DeviceCommandInvocation implements Warp10Converter<IDeviceCommandInvocation> {

    /**
     * Property for initiator
     */
    public static final String PROP_INITIATOR = "init";

    /**
     * Property for initiator id
     */
    public static final String PROP_INITIATOR_ID = "inid";

    /**
     * Property for target
     */
    public static final String PROP_TARGET = "targ";

    /**
     * Property for target id
     */
    public static final String PROP_TARGET_ID = "tgid";

    /**
     * Property for command token
     */
    public static final String PROP_COMMAND_ID = "cmid";

    /**
     * Property for parameter values
     */
    public static final String PROP_PARAMETER_VALUES = "pmvl";

    @Override
    public GTSInput convert(IDeviceCommandInvocation source) {
        return Warp10DeviceCommandInvocation.toGTS(source);
    }

    @Override
    public IDeviceCommandInvocation convert(GTSOutput source) {
        return Warp10DeviceCommandInvocation.fromGTS(source);
    }

    public static GTSInput toGTS(IDeviceCommandInvocation source) {
        GTSInput gtsInput = GTSInput.builder();
        Warp10DeviceCommandInvocation.toGTS(source, gtsInput);
        return gtsInput;
    }

    public static void toGTS(IDeviceCommandInvocation source, GTSInput target) {
        Warp10DeviceEvent.toGTS(source, target, false);
        target.setName(source.getDeviceAssignmentId().toString());
        target.setTs(source.getReceivedDate().getTime());

        Map labels = new HashMap<String, String>();
        labels.put(PROP_INITIATOR, source.getInitiator().name());
        labels.put(PROP_INITIATOR_ID, source.getInitiatorId());
        labels.put(PROP_TARGET, source.getTarget().name());
        labels.put(PROP_TARGET_ID, source.getTargetId());
        labels.put(PROP_COMMAND_ID, source.getDeviceCommandId());
        target.setLabels(labels);

        Map attributes = new HashMap<String, String>();
        for (String key : source.getParameterValues().keySet()) {
            attributes.put(key, source.getParameterValues().get(key));
        }

        target.setAttributes(attributes);
    }

    public static DeviceCommandInvocation fromGTS(GTSOutput source) {
        DeviceCommandInvocation deviceCommandInvocation = new DeviceCommandInvocation();
        Warp10DeviceCommandInvocation.fromGTS(source, deviceCommandInvocation);
        return deviceCommandInvocation;
    }

    public static void fromGTS(GTSOutput source, DeviceCommandInvocation target) {
        Warp10DeviceEvent.fromGTS(source, target, false);

        String initiatorName = source.getLabels().get(PROP_INITIATOR);
        String initiatorId = source.getLabels().get(PROP_INITIATOR_ID);
        String targetName = source.getLabels().get(PROP_TARGET);
        String targetId = source.getLabels().get(PROP_TARGET_ID);
        UUID commandId = UUID.fromString(source.getLabels().get(PROP_COMMAND_ID));

        if (initiatorName != null) {
            target.setInitiator(CommandInitiator.valueOf(initiatorName));
        }
        if (targetName != null) {
            target.setTarget(CommandTarget.valueOf(targetName));
        }
        target.setInitiatorId(initiatorId);
        target.setTargetId(targetId);
        target.setDeviceCommandId(commandId);

        Map<String, String> params = new HashMap<String, String>();

        if(source.getAttributes() != null && source.getAttributes().size() > 0) {
            for (String key : source.getAttributes().keySet()) {
                params.put(key, (String) source.getAttributes().get(key));
            }
        }
        target.setParameterValues(params);
    }

}
