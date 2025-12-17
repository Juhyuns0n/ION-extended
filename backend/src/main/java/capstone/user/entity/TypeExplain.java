package capstone.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_explain")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TypeExplain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "type", nullable = false, unique = true, length = 50)
    private String type;

    @Column(name = "explain", columnDefinition = "TEXT")
    private String explain;
}
