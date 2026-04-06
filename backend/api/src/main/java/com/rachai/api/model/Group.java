@Entity
@Table(name = "groups")
public class Group {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    private String name; 

    private String description; 

    @ManyToOne
    @JoinColumn(name = "owner_id") 
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"), 
        inverseJoinColumns = @JoinColumn(name = "user_id") 
    )

    private Set<User> members = new HashSet<>(); 

    // Construtor vazio
    public Group() {}

    // Getters e setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }
}