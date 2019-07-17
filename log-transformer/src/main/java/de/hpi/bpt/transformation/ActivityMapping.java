package de.hpi.bpt.transformation;

import java.util.Map;

public class ActivityMapping {

    public static final Map<String, String> ACTIVITY_MAPPING = Map.of(
            "Close ticket", "Ticket Closed",
            "Create Ticket in EasyVista", "Ticket Created",
            "Assign Ticket to Another Group", "Ticket Group Changed",
            "Solve ticket (technical closure of ticket)", "Ticket Resolved",
            "Re-open ticket", "Ticket Reopened",
            "Escalate ticket to another department", "Ticket Escalated",
            "Assign Ticket to Group", "Ticket Assigned to Group",
            "Notify collaborator", "Recipient notified",
            "Close the ticket (by the new Group)", "Processing ended",
            "Assign Ticket to Employee", "Ticket Assigned to Employee"
    );
}
