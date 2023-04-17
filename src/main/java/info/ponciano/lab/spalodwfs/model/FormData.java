package info.ponciano.lab.spalodwfs.model;

public class FormData {
    private String email;
    private String droplink;
    private String choice;
    private String info;
    private boolean newsletterAcceptance;
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getDroplink() {
        return droplink;
    }
    public void setDroplink(String droplink) {
        this.droplink = droplink;
    }
    public String getChoice() {
        return choice;
    }
    public void setChoice(String choice) {
        this.choice = choice;
    }
    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public boolean isNewsletterAcceptance() {
        return newsletterAcceptance;
    }
    public void setNewsletterAcceptance(boolean newsletterAcceptance) {
        this.newsletterAcceptance = newsletterAcceptance;
    }
    @Override
    public String toString() {
        return "FormData [email=" + email + ", droplink=" + droplink + ", choice=" + choice + ", info=" + info
                + ", newsletterAcceptance=" + newsletterAcceptance + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((droplink == null) ? 0 : droplink.hashCode());
        result = prime * result + ((choice == null) ? 0 : choice.hashCode());
        result = prime * result + ((info == null) ? 0 : info.hashCode());
        result = prime * result + (newsletterAcceptance ? 1231 : 1237);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FormData other = (FormData) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (droplink == null) {
            if (other.droplink != null)
                return false;
        } else if (!droplink.equals(other.droplink))
            return false;
        if (choice == null) {
            if (other.choice != null)
                return false;
        } else if (!choice.equals(other.choice))
            return false;
        if (info == null) {
            if (other.info != null)
                return false;
        } else if (!info.equals(other.info))
            return false;
        if (newsletterAcceptance != other.newsletterAcceptance)
            return false;
        return true;
    }


}
