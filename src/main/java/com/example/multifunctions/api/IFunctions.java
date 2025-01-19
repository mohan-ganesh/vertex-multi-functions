package com.example.multifunctions.api;

import com.google.protobuf.Struct;

public interface IFunctions {
    String createMember(Struct args);

    String searchMember(Struct args);

    String createAppointment(Struct args);

    String findOpenAppointments(Struct args);
}
