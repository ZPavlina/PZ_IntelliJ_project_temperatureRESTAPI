package pz_project_restapi.temperature.model;

import javax.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DetailForm {

    private float temperatureA;
    private float temperatureB;


}
