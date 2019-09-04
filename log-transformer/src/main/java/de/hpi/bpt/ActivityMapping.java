package de.hpi.bpt;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

public class ActivityMapping {

    private static final Map<String, String> MACIF_ACTIVITY_MAPPING = Map.of(
            "Close ticket", "Ticket Closed",
            "Create Ticket in EasyVista", "Ticket Created",
            "Assign Ticket to Another Group", "Ticket Group Changed",
            "Solve ticket (technical closure of ticket)", "Ticket Resolved",
            "Re-open ticket", "Ticket Reopened",
            "Escalate ticket to another department", "Ticket Escalated",
            "Assign Ticket to Group", "Ticket Assigned to Group",
            "Notify collaborator", "Recipient Notified",
            "Close the ticket (by the new Group)", "Processing ended",
            "Assign Ticket to Employee", "Ticket Assigned to Employee"
    );


    public static Map<String, String> get() {
//        return MACIF_ACTIVITY_MAPPING;
        return new IdentityMap();
    }

    static class IdentityMap implements Map<String, String> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object o) {
            return true;
        }

        @Override
        public boolean containsValue(Object o) {
            return true;
        }

        @Override
        public String get(Object o) {
            return String.valueOf(o);
        }

        @Override
        public String put(String s, String s2) {
            return null;
        }

        @Override
        public String remove(Object o) {
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> map) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<String> keySet() {
            return emptySet();
        }

        @Override
        public Collection<String> values() {
            return emptySet();
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return emptySet();
        }
    }
}