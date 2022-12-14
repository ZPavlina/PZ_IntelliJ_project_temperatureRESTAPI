package pz_project_restapi.temperature.service;

import java.time.*;
import java.util.*;
import pz_project_restapi.temperature.model.*;

public interface IItemService {


    List<Item> getAllItemsFromDatabase();

    TemperatureForm saveTemperatureLimits(TemperatureForm newLimits);

    TemperatureForm saveTemperatureAndTimeLimits(TemperatureForm newLimits);

    List<ItemLDT> longestPeriodByTemperature(List<ItemLDT> itemLDT, float temperatureA, float temperatureB);

    List<ItemLDT> longestPeriodByTemperatureAndTime(List<ItemLDT> itemLDT, float temperatureA,
                                                    float temperatureB, LocalTime timeX, LocalTime timeY);

    List<Item> getPeriodTe();

    public List<Item> getPeriodTeTi();



}
