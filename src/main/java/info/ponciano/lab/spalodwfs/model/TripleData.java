package info.ponciano.lab.spalodwfs.model;

public class TripleData {
        private String subject;
        private String predicate;
        private String object;

        
                // Getters and setters

        public TripleData(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }
        public String getSubject() {
            return subject;
        }
        public void setSubject(String subject) {
            this.subject = subject;
        }
        public String getPredicate() {
            return predicate;
        }
        public void setPredicate(String predicate) {
            this.predicate = predicate;
        }
        public String getObject() {
            return object;
        }
        public void setObject(String object) {
            this.object = object;
        }
        @Override
        public String toString() {
            return "TripleData [subject=" + subject + ", predicate=" + predicate + ", object=" + object + "]";
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((subject == null) ? 0 : subject.hashCode());
            result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
            result = prime * result + ((object == null) ? 0 : object.hashCode());
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
            TripleData other = (TripleData) obj;
            if (subject == null) {
                if (other.subject != null)
                    return false;
            } else if (!subject.equals(other.subject))
                return false;
            if (predicate == null) {
                if (other.predicate != null)
                    return false;
            } else if (!predicate.equals(other.predicate))
                return false;
            if (object == null) {
                if (other.object != null)
                    return false;
            } else if (!object.equals(other.object))
                return false;
            return true;
        }
    
        
    
      
    
    
}
